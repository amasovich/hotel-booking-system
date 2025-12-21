package ru.mifi.booking.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Discovery Server.
 *
 * <p>
 * Этот сервис является реестром микросервисов. Я запускаю его первым,
 * чтобы остальные сервисы (api-gateway, booking-service, hotel-service)
 * могли зарегистрироваться и находить друг друга по имени.
 * </p>
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    /**
     * Точка входа Spring Boot приложения.
     *
     * @param args аргументы командной строки (в рамках проекта не используются)
     */
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
