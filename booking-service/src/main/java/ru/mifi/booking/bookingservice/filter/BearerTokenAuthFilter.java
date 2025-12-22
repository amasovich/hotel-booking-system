package ru.mifi.booking.bookingservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.mifi.booking.bookingservice.entity.User;
import ru.mifi.booking.bookingservice.service.TokenService;

import java.io.IOException;
import java.util.List;

@Component
public class BearerTokenAuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public BearerTokenAuthFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring("Bearer ".length()).trim();

                User user = tokenService.findUserByToken(token);
                if (user != null) {
                    List<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                    // ВАЖНО: principal = userId как строка => auth.getName() тоже будет userId
                    var authentication = new UsernamePasswordAuthenticationToken(
                            String.valueOf(user.getId()),
                            null,
                            authorities
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
