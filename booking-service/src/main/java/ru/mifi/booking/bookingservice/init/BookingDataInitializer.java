package ru.mifi.booking.bookingservice.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mifi.booking.bookingservice.entity.User;
import ru.mifi.booking.bookingservice.entity.UserRole;
import ru.mifi.booking.bookingservice.repository.UserRepository;

/**
 * Инициализатор данных booking-service.
 *
 * <p>
 * Мне важно, чтобы проверяющий поднял проект и сразу смог:
 * - залогиниться (ADMIN/USER)
 * - получить JWT и тестировать остальные ручки
 * </p>
 *
 * <p>
 * Я создаю тестовых пользователей только если их ещё нет в базе.
 * Пароли хэширую тем же PasswordEncoder, что используется в AuthService.
 * </p>
 */
@Component
public class BookingDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BookingDataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BookingDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean createdAny = false;

        createdAny |= ensureUser(
                "Admin",
                "admin@local",
                "admin123",
                UserRole.ADMIN
        );

        createdAny |= ensureUser(
                "Ivan User",
                "user@local",
                "user123",
                UserRole.USER
        );

        if (createdAny) {
            // В учебном проекте это помогает быстро стартовать (особенно с H2 in-memory).
            log.info("Initial users are ready: admin@local/admin123 (ADMIN), user@local/user123 (USER)");
        } else {
            log.info("Initial users already exist. Skipping seeding.");
        }
    }

    /**
     * Создаёт пользователя, если email ещё не занят.
     *
     * @param name     имя пользователя
     * @param email    уникальный email
     * @param password сырой пароль (будет захэширован)
     * @param role     роль (ADMIN/USER)
     * @return true если пользователь был создан, иначе false
     */
    private boolean ensureUser(String name, String email, String password, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            return false;
        }

        User user = new User(
                null,
                name,
                email,
                passwordEncoder.encode(password),
                role
        );

        userRepository.save(user);
        log.info("Seeded user: {} ({})", email, role);
        return true;
    }
}
