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

                        // الطلبات التمهيدية للمتصفح
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // تسجيل الدخول والتحقق من الاتصال
                        .requestMatchers("/auth/**").permitAll()

                        // الوجبات والمنظمات متاحة للعرض
                        .requestMatchers(
                                HttpMethod.GET,
                                "/meals/**",
                                "/organisations/**"
                        ).permitAll()

                        // التسجيل العام
                        .requestMatchers(
                                HttpMethod.POST,
                                "/users"
                        ).permitAll()

                        // إدارة النظام للأدمن فقط
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // عمليات المطعم
                        .requestMatchers("/restaurant/**").hasRole("RESTAURANT")

                        // عمليات المندوب
                        .requestMatchers("/courier/**").hasRole("COURIER")

                        // قائمة المستخدمين للأدمن فقط
                        .requestMatchers(
                                HttpMethod.GET,
                                "/users"
                        ).hasRole("ADMIN")

                        // بيانات المستخدم تتطلب تسجيل الدخول
                        .requestMatchers("/users/**").authenticated()

                        // منع أي مسار غير محدد من الوصول العام
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}