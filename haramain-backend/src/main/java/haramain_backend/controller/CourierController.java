package haramain_backend.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import haramain_backend.model.MealRequest;
import haramain_backend.model.User;
import haramain_backend.repository.MealRequestRepository;
import haramain_backend.repository.UserRepository;

@RestController
@RequestMapping("/courier")
public class CourierController {

    private final MealRequestRepository mealRequestRepository;
    private final UserRepository userRepository;

    public CourierController(
            MealRequestRepository mealRequestRepository,
            UserRepository userRepository) {
        this.mealRequestRepository = mealRequestRepository;
        this.userRepository = userRepository;
    }

    // عرض الطلبات المسندة إلى المندوب المسجل دخوله
    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(Authentication authentication) {

        User courier = getAuthenticatedCourier(authentication);

        if (courier == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "حساب المندوب غير صالح أو غير نشط"));
        }

        List<Map<String, Object>> orders =
                mealRequestRepository.findByCourierId(courier.getUserId())
                        .stream()
                        .map(this::orderToMap)
                        .toList();

        return ResponseEntity.ok(orders);
    }

    // تحديث حالة الطلب إلى تم التوصيل
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable int id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        User courier = getAuthenticatedCourier(authentication);

        if (courier == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "حساب المندوب غير صالح أو غير نشط"));
        }

        String status = body.get("status") == null
                ? ""
                : body.get("status").toString().trim().toLowerCase(Locale.ROOT);

        if (!"delivered".equals(status)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "يمكن للمندوب تحديث الحالة إلى delivered فقط"));
        }

        MealRequest request = mealRequestRepository.findById(id).orElse(null);

        if (request == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "الطلب غير موجود"));
        }

        // التأكد أن الطلب مسند إلى المندوب الحالي
        if (request.getCourierId() == null
                || !request.getCourierId().equals(courier.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "هذا الطلب غير مسند إليك"));
        }

        String currentStatus = request.getStatus() == null
                ? ""
                : request.getStatus().trim().toLowerCase(Locale.ROOT);

        if (!"ready".equals(currentStatus)
                && !"confirmed".equals(currentStatus)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "لا يمكن إتمام التوصيل إلا للطلبات المؤكدة أو الجاهزة"));
        }

        request.setStatus("delivered");
        MealRequest savedRequest = mealRequestRepository.save(request);

        return ResponseEntity.ok(orderToMap(savedRequest));
    }

    private User getAuthenticatedCourier(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        try {
            int userId = Integer.parseInt(authentication.getName());

            User courier = userRepository.findById(userId).orElse(null);

            if (courier == null || !courier.isActive()) {
                return null;
            }

            if (courier.getRole() == null
                    || !"courier".equalsIgnoreCase(courier.getRole().trim())) {
                return null;
            }

            return courier;

        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Map<String, Object> orderToMap(MealRequest request) {

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("requestId", request.getRequestId());
        result.put("userId", request.getUserId());
        result.put("requestDate", request.getRequestDate());
        result.put("status", request.getStatus());
        result.put("organisationId", request.getRestaurantId());
        result.put("courierId", request.getCourierId());
        result.put("deliveryMethod", request.getDeliveryMethod());

        return result;
    }
}
