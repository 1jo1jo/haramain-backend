package haramain_backend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import haramain_backend.model.MealRequest;
import haramain_backend.model.User;
import haramain_backend.repository.MealRequestRepository;
import haramain_backend.repository.UserRepository;

@RestController
@RequestMapping("/restaurant")
public class RestaurantController {

    private final MealRequestRepository mealRequestRepository;
    private final UserRepository userRepository;

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("confirmed", "preparing", "ready");

    public RestaurantController(
            MealRequestRepository mealRequestRepository,
            UserRepository userRepository) {
        this.mealRequestRepository = mealRequestRepository;
        this.userRepository = userRepository;
    }

    // عرض طلبات المنظمة المرتبطة بحساب المطعم
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(Authentication authentication) {

        User restaurant = getAuthenticatedRestaurant(authentication);

        if (restaurant == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message",
                            "حساب المطعم غير موجود أو غير مرتبط بمنظمة"));
        }

        List<Map<String, Object>> orders = new ArrayList<>();

        for (MealRequest request :
                mealRequestRepository.findByRestaurantId(
                        restaurant.getOrganisationId())) {

            orders.add(orderToMap(request));
        }

        return ResponseEntity.ok(orders);
    }

    // تحديث حالة طلب تابع لمنظمة المطعم
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable int id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        User restaurant = getAuthenticatedRestaurant(authentication);

        if (restaurant == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message",
                            "حساب المطعم غير موجود أو غير مرتبط بمنظمة"));
        }

        String status = body.get("status");

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "يرجى تحديد حالة الطلب"));
        }

        status = status.trim().toLowerCase(Locale.ROOT);

        if (!ALLOWED_STATUSES.contains(status)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "حالة الطلب غير مسموحة للمطعم"));
        }

        MealRequest request = mealRequestRepository.findById(id).orElse(null);

        if (request == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "الطلب غير موجود"));
        }

        if (!restaurant.getOrganisationId().equals(request.getRestaurantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message",
                            "لا يمكنك تعديل طلب تابع لمنظمة أخرى"));
        }

        String currentStatus = request.getStatus();

        if (currentStatus == null
                || !(currentStatus.equalsIgnoreCase("pending")
                || currentStatus.equalsIgnoreCase("confirmed")
                || currentStatus.equalsIgnoreCase("preparing")
                || currentStatus.equalsIgnoreCase("ready"))) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "لا يمكن تحديث حالة الطلب الحالية"));
        }

        request.setStatus(status);

        MealRequest saved = mealRequestRepository.save(request);

        return ResponseEntity.ok(Map.of(
                "message", "تم تحديث حالة الطلب",
                "requestId", saved.getRequestId(),
                "status", saved.getStatus()
        ));
    }


// اختيار طريقة التوصيل: المطعم أو المندوب
    @PutMapping("/orders/{id}/assign")
    public ResponseEntity<?> assignCourier(
            @PathVariable int id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        User restaurant = getAuthenticatedRestaurant(authentication);

        if (restaurant == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message",
                            "حساب المطعم غير موجود أو غير مرتبط بمنظمة"));
        }

        MealRequest request = mealRequestRepository.findById(id).orElse(null);

        if (request == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "الطلب غير موجود"));
        }

        if (!restaurant.getOrganisationId().equals(request.getRestaurantId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message",
                            "لا يمكنك تعديل طلب تابع لمنظمة أخرى"));
        }

        String currentStatus = request.getStatus() == null
                ? ""
                : request.getStatus().trim().toLowerCase(Locale.ROOT);

        if (!"ready".equals(currentStatus)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "يجب تجهيز الطلب أولًا قبل اختيار طريقة التوصيل"));
        }

        Object rawMethod = body.get("deliveryMethod");

        String deliveryMethod = rawMethod == null
                ? ""
                : rawMethod.toString().trim().toLowerCase(Locale.ROOT);

        if (!deliveryMethod.equals("restaurant")
                && !deliveryMethod.equals("courier")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "طريقة التوصيل يجب أن تكون restaurant أو courier"));
        }

        if (deliveryMethod.equals("restaurant")) {

            request.setDeliveryMethod("restaurant");
            request.setCourierId(null);

        } else {

            Object rawCourierId = body.get("courierId");

            if (rawCourierId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message",
                                "يرجى تحديد رقم المندوب"));
            }

            int courierId;

            try {
                if (rawCourierId instanceof Number number) {
                    courierId = number.intValue();
                } else {
                    courierId = Integer.parseInt(
                            rawCourierId.toString().trim());
                }
            } catch (NumberFormatException exception) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message",
                                "رقم المندوب غير صحيح"));
            }

            User courier = userRepository.findById(courierId).orElse(null);

            if (courier == null
                    || !courier.isActive()
                    || courier.getRole() == null
                    || !courier.getRole().equalsIgnoreCase("courier")) {

                return ResponseEntity.badRequest()
                        .body(Map.of("message",
                                "حساب المندوب غير موجود أو غير نشط"));
            }

            request.setDeliveryMethod("courier");
            request.setCourierId(courierId);
        }

        MealRequest saved = mealRequestRepository.save(request);

        return ResponseEntity.ok(Map.of(
                "message", "تم تحديد طريقة التوصيل بنجاح",
                "requestId", saved.getRequestId(),
                "deliveryMethod", saved.getDeliveryMethod(),
                "courierId", saved.getCourierId() == null
                        ? 0 : saved.getCourierId(),
                "status", saved.getStatus()
        ));
    }



    private User getAuthenticatedRestaurant(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return null;
        }

        int userId;

        try {
            userId = Integer.parseInt(authentication.getName());
        } catch (NumberFormatException exception) {
            return null;
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null
                || !user.isActive()
                || user.getRole() == null
                || !user.getRole().equalsIgnoreCase("restaurant")
                || user.getOrganisationId() == null) {
            return null;
        }

        return user;
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