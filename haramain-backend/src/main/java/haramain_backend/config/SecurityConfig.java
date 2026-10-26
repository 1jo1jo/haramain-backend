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

                    configuration.setAllowedOriginPatterns(List.of("*"));
                    configuration.setAllowedMethods(
                            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    configuration.setAllowedHeaders(List.of("*"));

                    return configuration;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // طلبات OPTIONS
                        .requestMatchers(
                                HttpMethod.OPTIONS, "/**"
                        ).permitAll()

                        // تسجيل الدخول
                        .requestMatchers("/auth/**").permitAll()

                        // وجبات المطعم الخاصة: للمطعم فقط
                        .requestMatchers(
                                HttpMethod.GET,
                                "/meals/my-meals"
                        ).hasRole("RESTAURANT")

                        // عرض الوجبات والمنظمات للجميع
                        .requestMatchers(
                                HttpMethod.GET,
                                "/meals/**",
                                "/organisations/**"
                        ).permitAll()

                        // إضافة الوجبات للمطعم فقط
                        .requestMatchers(
                                HttpMethod.POST,
                                "/meals/**"
                        ).hasRole("RESTAURANT")

                        // تعديل الوجبات للمطعم فقط
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/meals/**"
                        ).hasRole("RESTAURANT")

                        // حذف الوجبات للمطعم فقط
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/meals/**"
                        ).hasRole("RESTAURANT")

                        // التسجيل العام
                        .requestMatchers(
                                HttpMethod.POST,
                                "/users"
                        ).permitAll()

                        // إدارة النظام
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // إدارة طلبات المطعم
                        .requestMatchers("/restaurant/**")
                        .hasRole("RESTAURANT")

                        // عمليات المندوب
                        .requestMatchers("/courier/**")
                        .hasRole("COURIER")

                        // قائمة المستخدمين للأدمن فقط
                        .requestMatchers(
                                HttpMethod.GET,
                                "/users"
                        ).hasRole("ADMIN")

                        // بيانات المستخدم
                        .requestMatchers("/users/**").authenticated()

                        // بقية المسارات تتطلب تسجيل الدخول
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}