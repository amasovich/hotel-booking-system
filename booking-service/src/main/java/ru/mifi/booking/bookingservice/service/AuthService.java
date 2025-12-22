package ru.mifi.booking.bookingservice.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.mifi.booking.common.exception.ConflictException;
import ru.mifi.booking.common.exception.UnauthorizedException;
import ru.mifi.booking.bookingservice.dto.AuthRequest;
import ru.mifi.booking.bookingservice.dto.AuthResponse;
import ru.mifi.booking.bookingservice.dto.RegisterRequest;
import ru.mifi.booking.bookingservice.entity.User;
import ru.mifi.booking.bookingservice.entity.UserRole;
import ru.mifi.booking.bookingservice.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User(null, req.name(), req.email(), encoder.encode(req.password()), UserRole.USER);
        User saved = userRepository.save(user);

        String token = tokenService.issueToken(saved);
        return new AuthResponse(saved.getId(), token, saved.getRole().name());
    }

    public AuthResponse auth(AuthRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = tokenService.issueToken(user);
        return new AuthResponse(user.getId(), token, user.getRole().name());
    }
}
