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

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {

        return userRepository.findAll().stream()
                .map(this::userToMap)
                .toList();
    }

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

    @PostMapping("/users")
    public Map<String, Object> createUser(
            @RequestBody User user) {

        if (user.getPassword() == null
                || user.getPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "Password is required");
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        return userToMap(savedUser);
    }

    private Map<String, Object> userToMap(User user) {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("userId", user.getUserId());
        data.put("fullName", user.getFullName());
        data.put("username", user.getUsername());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());

        return data;
    }
}
