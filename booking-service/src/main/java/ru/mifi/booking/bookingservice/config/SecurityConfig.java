package ru.mifi.booking.bookingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.mifi.booking.bookingservice.filter.BearerTokenAuthFilter;

/**
 * Security-конфигурация booking-service.
 *
 * <p>
 * На этом шаге я НЕ заставляю клиента логиниться средствами Spring Security,
 * а только даю возможность прокинуть Authentication через Bearer-токен,
 * чтобы существующий BookingController мог работать без переписывания.
 * </p>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   BearerTokenAuthFilter bearerTokenAuthFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .logout(l -> l.disable())
                .anonymous(a -> a.disable()) // важно: без токена Authentication будет null
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) // ошибки формируем сами через ApiException
                .addFilterBefore(bearerTokenAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
