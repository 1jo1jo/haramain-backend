package haramain_backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import haramain_backend.model.User;
import haramain_backend.repository.UserRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        User user = userRepository.findAll().stream()
                .filter(u ->
                        identifier.equalsIgnoreCase(u.getUsername())
                                || (u.getEmail() != null
                                && identifier.equalsIgnoreCase(u.getEmail())))
                .findFirst()
                .orElse(null);

        if (user == null || user.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "بيانات تسجيل الدخول غير صحيحة")
            );
        }

        String storedPassword = user.getPassword();

        boolean passwordMatches;

        if (storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")) {
            passwordMatches =
                    passwordEncoder.matches(password, storedPassword);
        } else {
            // دعم مؤقت للحسابات التجريبية القديمة فقط
            passwordMatches = password.equals(storedPassword);
        }

        if (!passwordMatches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "بيانات تسجيل الدخول غير صحيحة")
            );
        }

        return ResponseEntity.ok(Map.of(
                "message", "تم تسجيل الدخول بنجاح",
                "userId", user.getUserId(),
                "fullName", user.getFullName(),
                "username", user.getUsername(),
                "email", user.getEmail()
        ));
    }
}
