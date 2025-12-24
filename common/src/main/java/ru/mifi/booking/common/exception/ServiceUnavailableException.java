package ru.mifi.booking.common.exception;

/**
 * 503 Service Unavailable — зависимость недоступна (таймаут/ошибка удалённого сервиса).
 */
public class ServiceUnavailableException extends ApiException {

    public ServiceUnavailableException(String message) {
        super(503, message);
    }
}