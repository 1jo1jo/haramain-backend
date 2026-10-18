package haramain_backend.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import haramain_backend.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration:86400000}")
    private long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // إنشاء رمز دخول للمستخدم
    public String generateToken(User user) {

        Date now = new Date();
        Date expiryDate = new Date(
                now.getTime() + expiration
        );

        String role = user.getRole() != null
                ? user.getRole()
                : "user";

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // استخراج بيانات الرمز بعد التحقق من توقيعه
    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // استخراج رقم المستخدم
    public int extractUserId(String token) {

        return Integer.parseInt(
                extractClaims(token).getSubject()
        );
    }

    // استخراج صلاحية المستخدم
    public String extractRole(String token) {

        return extractClaims(token).get("role", String.class);
    }

    // التحقق من صلاحية الرمز
    public boolean isTokenValid(String token) {

        try {
            Claims claims = extractClaims(token);

            return claims.getSubject() != null
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }
}