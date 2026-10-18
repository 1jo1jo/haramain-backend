package haramain_backend.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import haramain_backend.model.User;
import haramain_backend.repository.UserRepository;

@RestController
public class UserController {


    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // جلب جميع المستخدمين - المسار الحالي
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::userToMap)
                .toList();
    }

    // جلب مستخدم بواسطة رقمه - المسار الحالي
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(
            @PathVariable Integer id) {

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message", "المستخدم غير موجود"
                    ));
        }

        return ResponseEntity.ok(userToMap(user.get()));
    }

// إنشاء حساب مستخدم عادي
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {

        if (user.getPassword() == null
                || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "كلمة المرور مطلوبة"));
        }

        if (user.getEmail() == null
                || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "البريد الإلكتروني مطلوب"));
        }

        if (userRepository.findAll().stream()
                .anyMatch(existing ->
                        existing.getEmail() != null
                                && existing.getEmail().equalsIgnoreCase(
                                user.getEmail().trim()))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message", "البريد الإلكتروني مسجل مسبقًا"));
        }

        if (user.getFullName() == null
                || user.getFullName().isBlank()
                || user.getUsername() == null
                || user.getUsername().isBlank()
                || user.getPhone() == null
                || user.getPhone().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "الاسم واسم المستخدم ورقم الجوال مطلوبة"));
        }

        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setFullName(user.getFullName().trim());
        user.setUsername(user.getUsername().trim());
        user.setPhone(user.getPhone().trim());

        // منع التسجيل العام من إنشاء حساب بصلاحيات مرتفعة
        user.setRole("user");

        user.setPassword(
                passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userToMap(savedUser));
    }


    // تجهيز بيانات المستخدم دون إظهار كلمة المرور
    private Map<String, Object> userToMap(User user) {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("userId", user.getUserId());
        data.put("fullName", user.getFullName());
        data.put("username", user.getUsername());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put(
                "role",
                user.getRole() != null
                        ? user.getRole()
                        : "user"
        );

        return data;
    }


}
