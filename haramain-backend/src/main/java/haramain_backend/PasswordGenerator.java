package haramain_backend;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


        String newPassword = "JjMySQL@2026!";
        String hashedPassword = encoder.encode(newPassword);

        System.out.println(hashedPassword);
    }


}
