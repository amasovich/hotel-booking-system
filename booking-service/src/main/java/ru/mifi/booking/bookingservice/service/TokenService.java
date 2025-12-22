package ru.mifi.booking.bookingservice.service;

import org.springframework.stereotype.Service;
import ru.mifi.booking.bookingservice.entity.User;
import ru.mifi.booking.bookingservice.entity.UserToken;
import ru.mifi.booking.bookingservice.repository.UserTokenRepository;

import java.time.Instant;
import java.util.UUID;

@Service
public class TokenService {

    private final UserTokenRepository tokenRepository;

    public TokenService(UserTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String issueToken(User user) {
        String token = UUID.randomUUID().toString();
        tokenRepository.save(new UserToken(null, token, user, Instant.now()));
        return token;
    }

    public User findUserByToken(String token) {
        return tokenRepository.findByToken(token)
                .map(UserToken::getUser)
                .orElse(null);
    }
}
