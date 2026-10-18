 package haramain_backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration =
                            new CorsConfiguration();

                    configuration.setAllowedOriginPatterns(
                            List.of("*"));

                    configuration.setAllowedMethods(
                            List.of(
                                    "GET",
                                    "POST",
                                    "PUT",
                                    "DELETE",
                                    "OPTIONS"
                            ));

                    configuration.setAllowedHeaders(
                            List.of("*"));

                    return configuration;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // تسجيل الدخول
                        .requestMatchers("/auth/**").permitAll()

                        // عرض الوجبات والمنظمات
                        .requestMatchers(
                                "/meals/**",
                                "/organisations/**"
                        ).permitAll()

                        // إنشاء حساب جديد فقط بدون تسجيل دخول
                        .requestMatchers(
                                HttpMethod.POST,
                                "/users"
                        ).permitAll()

                        // عرض قائمة المستخدمين للأدمن فقط
                        .requestMatchers(
                                HttpMethod.GET,
                                "/users"
                        ).hasRole("ADMIN")

                        // عرض بيانات مستخدم يتطلب تسجيل الدخول
                        .requestMatchers(
                                HttpMethod.GET,
                                "/users/*"
                        ).authenticated()

                        // جميع مسارات الإدارة للأدمن فقط
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // بقية المسارات
                        .anyRequest().permitAll()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

