package haramain_backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "restaurant1";
        String hashedPassword = encoder.encode(password);

        System.out.println(hashedPassword);
    }
}
