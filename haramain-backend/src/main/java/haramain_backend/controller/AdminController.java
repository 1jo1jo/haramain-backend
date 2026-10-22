package haramain_backend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import haramain_backend.model.MealRequest;
import haramain_backend.model.Organisation;
import haramain_backend.model.User;
import haramain_backend.repository.MealRequestRepository;
import haramain_backend.repository.OrganisationRepository;
import haramain_backend.repository.UserRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final MealRequestRepository mealRequestRepository;
    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private static final Set<String> ALLOWED_ROLES =
            Set.of("user", "restaurant", "courier", "admin");

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("pending", "confirmed", "preparing", "ready", "delivered");

    public AdminController(
            MealRequestRepository mealRequestRepository,
            UserRepository userRepository,
            OrganisationRepository organisationRepository) {

        this.mealRequestRepository = mealRequestRepository;
        this.userRepository = userRepository;
        this.organisationRepository = organisationRepository;
    }

    // عرض جميع الطلبات
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {

        List<Map<String, Object>> orders = new ArrayList<>();

        for (MealRequest request : mealRequestRepository.findAll()) {

            Map<String, Object> order = new LinkedHashMap<>();

            order.put("requestId", request.getRequestId());
            order.put("userId", request.getUserId());
            order.put("requestDate", request.getRequestDate());
            order.put("status", request.getStatus());
            order.put("organisationId", request.getRestaurantId());
            order.put("courierId", request.getCourierId());
            order.put("deliveryMethod", request.getDeliveryMethod());

            orders.add(order);
        }

        return ResponseEntity.ok(orders);
    }

    // تحديث حالة الطلب
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "يرجى إدخال حالة الطلب"));
        }

        status = status.trim().toLowerCase(Locale.ROOT);

        if (!ALLOWED_STATUSES.contains(status)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "حالة الطلب غير معتمدة"));
        }

        MealRequest request =
                mealRequestRepository.findById(id).orElse(null);

        if (request == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "الطلب غير موجود"));
        }

        request.setStatus(status);

        MealRequest savedRequest =
                mealRequestRepository.save(request);

        return ResponseEntity.ok(Map.of(
                "message", "تم تحديث حالة الطلب بنجاح",
                "requestId", savedRequest.getRequestId(),
                "status", savedRequest.getStatus()
        ));
    }

    // عرض جميع المستخدمين
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {

        List<Map<String, Object>> users = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            users.add(userToMap(user));
        }

        return ResponseEntity.ok(users);
    }

    // تفعيل حساب أو إيقافه
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable int id,
            @RequestBody Map<String, Boolean> body) {

        Boolean isActive = body.get("isActive");

        if (isActive == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "يرجى تحديد حالة الحساب"));
        }

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "المستخدم غير موجود"));
        }

        user.setActive(isActive);

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", isActive
                        ? "تم تفعيل الحساب بنجاح"
                        : "تم إيقاف الحساب بنجاح",
                "userId", savedUser.getUserId(),
                "isActive", savedUser.isActive()
        ));
    }

    // إنشاء حساب مستخدم أو مطعم أو مندوب
    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @RequestBody Map<String, Object> body) {

        String fullName = getString(body, "fullName");
        String username = getString(body, "username");
        String phone = getString(body, "phone");
        String email = getString(body, "email");
        String password = getString(body, "password");
        String role = getString(body, "role");

        if (isBlank(fullName)
                || isBlank(username)
                || isBlank(phone)
                || isBlank(email)
                || isBlank(password)) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "يرجى تعبئة جميع الحقول المطلوبة"));
        }

        fullName = fullName.trim();
        username = username.trim();
        phone = phone.trim();
        email = email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", "البريد الإلكتروني مستخدم بالفعل"));
        }

        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", "اسم المستخدم مستخدم بالفعل"));
        }

        if (role == null || role.isBlank()) {
            role = "user";
        } else {
            role = role.trim().toLowerCase(Locale.ROOT);
        }

        if (!ALLOWED_ROLES.contains(role)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "صلاحية المستخدم غير صحيحة"));
        }

        Integer organisationId = null;

        if ("restaurant".equals(role)) {

            Object rawOrganisationId = body.get("organisationId");

            if (rawOrganisationId == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("message",
                                "يجب تحديد المنظمة التابعة لحساب المطعم"));
            }

            try {
                if (rawOrganisationId instanceof Number number) {
                    organisationId = number.intValue();
                } else {
                    organisationId = Integer.parseInt(
                            rawOrganisationId.toString().trim());
                }
            } catch (NumberFormatException exception) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "رقم المنظمة غير صحيح"));
            }

            if (organisationId <= 0) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "رقم المنظمة غير صحيح"));
            }

            Organisation organisation =
                    organisationRepository.findById(organisationId).orElse(null);

            if (organisation == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "المنظمة المحددة غير موجودة"));
            }
        }

        User user = new User();

        user.setFullName(fullName);
        user.setUsername(username);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        user.setOrganisationId(organisationId);

        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                userToMap(savedUser));
    }

    // تجهيز بيانات المستخدم دون إظهار كلمة المرور
    private Map<String, Object> userToMap(User user) {

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("userId", user.getUserId());
        result.put("fullName", user.getFullName());
        result.put("username", user.getUsername());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("role",
                user.getRole() != null ? user.getRole() : "user");
        result.put("isActive", user.isActive());
        result.put("organisationId", user.getOrganisationId());

        return result;
    }

    private String getString(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? null : value.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}