 package haramain_backend.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import haramain_backend.model.MealRequest;
import haramain_backend.model.User;
import haramain_backend.repository.MealRequestRepository;
import haramain_backend.repository.UserRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final MealRequestRepository mealRequestRepository;
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private static final Set<String> ALLOWED_ROLES =
            Set.of("user", "admin", "distributor", "organization");

    public AdminController(
            MealRequestRepository mealRequestRepository,
            UserRepository userRepository) {

        this.mealRequestRepository = mealRequestRepository;
        this.userRepository = userRepository;
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

            orders.add(order);
        }

        return ResponseEntity.ok(orders);
    }

    // تحديث حالة طلب
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "يرجى إدخال حالة الطلب")
            );
        }

        MealRequest request =
                mealRequestRepository.findById(id).orElse(null);

        if (request == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "الطلب غير موجود")
            );
        }

        request.setStatus(status.trim());

        MealRequest savedRequest =
                mealRequestRepository.save(request);

        return ResponseEntity.ok(Map.of(
                "message", "تم تحديث حالة الطلب بنجاح",
                "requestId", savedRequest.getRequestId(),
                "status", savedRequest.getStatus()
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {

        List<Map<String, Object>> users = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            users.add(userToMap(user));
        }

        return ResponseEntity.ok(users);
    }

    // إنشاء مستخدم جديد
    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @RequestBody Map<String, String> body) {

        String fullName = body.get("fullName");
        String username = body.get("username");
        String phone = body.get("phone");
        String email = body.get("email");
        String password = body.get("password");
        String role = body.get("role");

        if (isBlank(fullName)
                || isBlank(username)
                || isBlank(phone)
                || isBlank(email)
                || isBlank(password)) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "يرجى تعبئة جميع الحقول المطلوبة")
            );
        }

        final String normalizedEmail =
                email.trim().toLowerCase();

        username = username.trim();

        if (userRepository.findAll().stream()
                .anyMatch(u -> normalizedEmail.equalsIgnoreCase(u.getEmail()))) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", "البريد الإلكتروني مستخدم بالفعل")
            );
        }

        if (role == null || role.isBlank()) {
            role = "user";
        } else {
            role = role.trim().toLowerCase();
        }

        if (!ALLOWED_ROLES.contains(role)) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "صلاحية المستخدم غير صحيحة")
            );
        }

        User user = new User();

        user.setFullName(fullName.trim());
        user.setUsername(username);
        user.setPhone(phone.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                userToMap(savedUser)
        );
    }

    private Map<String, Object> userToMap(User user) {

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("userId", user.getUserId());
        result.put("fullName", user.getFullName());
        result.put("username", user.getUsername());
        result.put("phone", user.getPhone());
        result.put("email", user.getEmail());
        result.put("role",
                user.getRole() != null ? user.getRole() : "user");

        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

