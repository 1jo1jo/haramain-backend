package haramain_backend.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import haramain_backend.config.JwtService;
import haramain_backend.model.User;
import haramain_backend.repository.UserRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JdbcTemplate jdbcTemplate;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthController(
            UserRepository userRepository,
            JwtService jwtService,
            JdbcTemplate jdbcTemplate) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db-check")
    public Map<String, Object> dbCheck() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put(
                "connection",
                jdbcTemplate.queryForMap(
                        "SELECT DATABASE() AS dbName, "
                                + "@@hostname AS hostName, "
                                + "@@port AS port"
                )
        );

        result.put(
                "userColumns",
                jdbcTemplate.queryForList(
                        "SHOW COLUMNS FROM `user`"
                )
        );

        return result;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> request) {

        String identifier = request.get("identifier");
        String password = request.get("password");

        if (identifier == null || identifier.isBlank()
                || password == null || password.isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of("message", "يرجى إدخال بيانات تسجيل الدخول")
            );
        }

        String normalizedIdentifier = identifier.trim();

        User user = userRepository
                .findByEmailIgnoreCase(normalizedIdentifier)
                .or(() -> userRepository
                        .findByUsernameIgnoreCase(normalizedIdentifier))
                .orElse(null);

        if (user == null || user.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "بيانات تسجيل الدخول غير صحيحة")
            );
        }

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of(
                            "message",
                            "الحساب موقوف، يرجى التواصل مع الإدارة"
                    )
            );
        }

        String storedPassword = user.getPassword();

        boolean isBcryptPassword =
                storedPassword.startsWith("$2a$")
                        || storedPassword.startsWith("$2b$")
                        || storedPassword.startsWith("$2y$");

        boolean passwordMatches;

        if (isBcryptPassword) {
            passwordMatches =
                    passwordEncoder.matches(password, storedPassword);
        } else {
            // توافق مؤقت مع الحسابات التجريبية القديمة
            passwordMatches = password.equals(storedPassword);

            if (passwordMatches) {
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
            }
        }

        if (!passwordMatches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "بيانات تسجيل الدخول غير صحيحة")
            );
        }

        String role = user.getRole() != null
                ? user.getRole()
                : "user";

        String token = jwtService.generateToken(user);

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("message", "تم تسجيل الدخول بنجاح");
        response.put("userId", user.getUserId());
        response.put(
                "fullName",
                user.getFullName() != null ? user.getFullName() : ""
        );
        response.put(
                "username",
                user.getUsername() != null ? user.getUsername() : ""
        );
        response.put(
                "email",
                user.getEmail() != null ? user.getEmail() : ""
        );
        response.put("role", role);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }
}
