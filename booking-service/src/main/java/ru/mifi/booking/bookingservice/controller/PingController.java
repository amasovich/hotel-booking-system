package ru.mifi.booking.bookingservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Простой контроллер для проверки доступности сервиса.
 *
 * <p>
 * Я использую этот эндпойнт, чтобы быстро проверить:
 * 1) сервис стартует,
 * 2) Gateway может проксировать запросы,
 * 3) Eureka-регистрация работает.
 * </p>
 */
@RestController
public class PingController {

    /**
     * Проверка доступности.
     *
     * @return строка-ответ от booking-service
     */
    @GetMapping("/ping")
    public String ping() {
        return "booking-service: OK";
    }
}
