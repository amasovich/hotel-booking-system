package ru.mifi.booking.bookingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mifi.booking.bookingservice.entity.UserToken;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByToken(String token);
}
