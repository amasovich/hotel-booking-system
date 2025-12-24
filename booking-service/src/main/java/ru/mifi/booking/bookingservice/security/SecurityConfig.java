package ru.mifi.booking.bookingservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Security-конфигурация booking-service.
 *
 * <p>
 * Я настраиваю сервис как Resource Server:
 * - принимаю JWT, подписанные нашим секретом (HS256)
 * - беру роль из claim "role" и превращаю в ROLE_<ROLE>
 * - ограничиваю доступ к endpoints по ролям
 * </p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final String secret;

    public SecurityConfig(@Value("${security.jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("security.jwt.secret is empty. JWT validation cannot work without secret.");
        }
        this.secret = secret;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           RestAuthenticationEntryPoint authenticationEntryPoint,
                                           RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                // REST API: без сессий
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CSRF выключаем (у нас stateless API)
                .csrf(csrf -> csrf.disable())

                // отключаем лишние дефолтные механики
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(auth -> auth
                        // Actuator (по желанию можно тоже закрыть)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Swagger / OpenAPI (чтобы проверяющий мог открыть Swagger UI без токена)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Публичные endpoints (логин/регистрация)
                        .requestMatchers("/api/user/register", "/api/user/auth").permitAll()

                        // ADMIN-only операции над пользователями
                        .requestMatchers(HttpMethod.POST, "/api/user").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/user").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/user").hasRole("ADMIN")

                        // Публичный API бронирований: только аутентифицированные USER|ADMIN
                        .requestMatchers("/api/booking/**", "/api/bookings/**").hasAnyRole("USER", "ADMIN")

                        // Всё остальное — только с валидным JWT
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    /**
     * Декодер JWT для HS256 (симметричный секрет).
     * Секрет должен совпадать у всех сервисов, которые проверяют эти токены.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")
        ).build();
    }

    /**
     * Конвертация claim "role": "USER" -> authority "ROLE_USER"
     * (и аналогично ADMIN/SERVICE).
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            if (role == null || role.isBlank()) {
                return List.of();
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}