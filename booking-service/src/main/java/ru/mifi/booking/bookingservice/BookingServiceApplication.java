package ru.mifi.booking.bookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Booking Service.
 *
 * <p>
 * Этот микросервис будет отвечать за пользователей, аутентификацию и бронирования.
 * На текущем шаге я поднимаю минимальный каркас сервиса и регистрирую его в Eureka.
 * </p>
 */
@SpringBootApplication
public class BookingServiceApplication {

    /**
     * Точка входа Spring Boot приложения.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
