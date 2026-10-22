package haramain_backend.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

    // جلب جميع المستخدمين - للأدمن فقط حسب SecurityConfig
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::userToMap)
                .toList();
    }

    // جلب بيانات المستخدم
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "يرجى تسجيل الدخول"));
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        Integer authenticatedUserId;

        try {
            authenticatedUserId = Integer.valueOf(
                    authentication.getName());
        } catch (NumberFormatException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "بيانات تسجيل الدخول غير صالحة"));
        }

        // المستخدم العادي لا يستطيع الاطلاع على بيانات مستخدم آخر
        if (!isAdmin && !authenticatedUserId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "ليس لديك صلاحية لعرض بيانات هذا المستخدم"));
        }

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "المستخدم غير موجود"));
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

        if (user.getFullName() == null
                || user.getFullName().isBlank()
                || user.getUsername() == null
                || user.getUsername().isBlank()
                || user.getPhone() == null
                || user.getPhone().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "الاسم واسم المستخدم ورقم الجوال مطلوبة"));
        }

        String email = user.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "البريد الإلكتروني مسجل مسبقًا"));
        }

        user.setEmail(email);
        user.setFullName(user.getFullName().trim());
        user.setUsername(user.getUsername().trim());
        user.setPhone(user.getPhone().trim());

        // التسجيل العام ينشئ حساب مستخدم عادي فقط
        user.setRole("user");
        user.setActive(true);

        // لا تسمحي للمستخدم بتعيين منظمة بنفسه أثناء التسجيل العام
        user.setOrganisationId(null);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

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
        data.put("role",
                user.getRole() != null ? user.getRole() : "user");
        data.put("isActive", user.isActive());
        data.put("organisationId", user.getOrganisationId());

        return data;
    }
}
