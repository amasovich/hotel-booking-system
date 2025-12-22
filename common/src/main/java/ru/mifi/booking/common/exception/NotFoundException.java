package ru.mifi.booking.common.exception;

/**
 * 404 Not Found — сущность не найдена.
 */
public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(404, message);
    }
}
