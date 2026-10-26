package haramain_backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import haramain_backend.dto.CreateOrderRequest;
import haramain_backend.model.Meal;
import haramain_backend.model.MealRequest;
import haramain_backend.model.Payment;
import haramain_backend.model.RequestDetail;
import haramain_backend.repository.MealRepository;
import haramain_backend.repository.MealRequestRepository;
import haramain_backend.repository.PaymentRepository;
import haramain_backend.repository.RequestDetailRepository;
import haramain_backend.repository.UserRepository;

@Service
public class OrderService {

    private final MealRequestRepository mealRequestRepository;
    private final RequestDetailRepository requestDetailRepository;
    private final MealRepository mealRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public OrderService(
            MealRequestRepository mealRequestRepository,
            RequestDetailRepository requestDetailRepository,
            MealRepository mealRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository) {

        this.mealRequestRepository = mealRequestRepository;
        this.requestDetailRepository = requestDetailRepository;
        this.mealRepository = mealRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResult createOrder(
            CreateOrderRequest request,
            int authenticatedUserId) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }

        if (authenticatedUserId <= 0
                || !userRepository.existsById(authenticatedUserId)) {
            throw new IllegalArgumentException("User not found");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one meal is required");
        }

        if (request.getPaymentMethod() == null
                || request.getPaymentMethod().isBlank()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        if (request.getDeliveryMethod() != null
                && !request.getDeliveryMethod().isBlank()
                && !request.getDeliveryMethod().equals("restaurant")
                && !request.getDeliveryMethod().equals("courier")) {
            throw new IllegalArgumentException("Invalid delivery method");
        }

        LocalDateTime now = LocalDateTime.now();

        MealRequest mealRequest = new MealRequest();
        mealRequest.setUserId(authenticatedUserId);
        mealRequest.setRequestDate(now);
        mealRequest.setStatus("PENDING");
        mealRequest.setDeliveryMethod(request.getDeliveryMethod());

        mealRequest = mealRequestRepository.saveAndFlush(mealRequest);

        int requestId = mealRequest.getRequestId();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<Meal> mealsToUpdate = new ArrayList<>();
        List<RequestDetail> details = new ArrayList<>();

        Integer restaurantOrganisationId = null;

        for (CreateOrderRequest.OrderItem item : request.getItems()) {

            if (item == null || item.getMealId() <= 0 || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Invalid meal or quantity");
            }

            Meal meal = mealRepository.findById(item.getMealId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Meal not found: " + item.getMealId()));
            // تحديد المنظمة المسؤولة عن الوجبة
            if (restaurantOrganisationId == null) {
                restaurantOrganisationId = meal.getOrganisationID();
            } else if (restaurantOrganisationId != meal.getOrganisationID()) {
                throw new IllegalArgumentException(
                        "All meals in one order must belong to the same restaurant");
            }

            if (meal.getQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for meal: " + item.getMealId());
            }

            BigDecimal unitPrice = BigDecimal.valueOf(meal.getPrice());
            BigDecimal lineTotal = unitPrice.multiply(
                    BigDecimal.valueOf(item.getQuantity()));

            subtotal = subtotal.add(lineTotal);

            RequestDetail detail = new RequestDetail();
            detail.setRequestId(requestId);
            detail.setMealId(meal.getMealID());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(unitPrice.doubleValue());
            detail.setTotalAmount(lineTotal.doubleValue());

            details.add(detail);

            meal.setQuantity(meal.getQuantity() - item.getQuantity());
            mealsToUpdate.add(meal);
        }

        if (restaurantOrganisationId == null) {
            throw new IllegalArgumentException(
                    "Could not determine the restaurant for this order");
        }

        mealRequest.setRestaurantId(restaurantOrganisationId);
        mealRequestRepository.save(mealRequest);

        requestDetailRepository.saveAll(details);
        mealRepository.saveAll(mealsToUpdate);

        Payment payment = new Payment();
        payment.setRequestId(requestId);
        payment.setDonationID(null);
        payment.setAmount(subtotal.doubleValue());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentDate(now);

        // سجل تجريبي فقط، وليس تأكيدًا على خصم المبلغ.
        payment.setPaymentStatus("PENDING");

        paymentRepository.save(payment);

        return new OrderResult(
                requestId,
                subtotal.doubleValue(),
                "PENDING",
                "Order created. Payment is pending confirmation."
        );
    }

    public static class OrderResult {

        private final int requestId;
        private final double totalAmount;
        private final String status;
        private final String message;

        public OrderResult(
                int requestId,
                double totalAmount,
                String status,
                String message) {

            this.requestId = requestId;
            this.totalAmount = totalAmount;
            this.status = status;
            this.message = message;
        }

        public int getRequestId() {
            return requestId;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}

