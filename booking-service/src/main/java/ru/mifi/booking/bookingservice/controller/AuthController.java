package ru.mifi.booking.bookingservice.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.mifi.booking.bookingservice.dto.AuthRequest;
import ru.mifi.booking.bookingservice.dto.AuthResponse;
import ru.mifi.booking.bookingservice.dto.RegisterRequest;
import ru.mifi.booking.bookingservice.service.AuthService;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/auth")
    public AuthResponse auth(@Valid @RequestBody AuthRequest req) {
        return authService.auth(req);
    }
}
