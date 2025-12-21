package ru.mifi.booking.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway.
 *
 * <p>
 * Этот сервис является единой точкой входа в систему.
 * На текущем шаге я настраиваю маршрутизацию в booking-service и hotel-service
 * через Eureka (lb://...).
 * </p>
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * Точка входа Spring Boot приложения.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
