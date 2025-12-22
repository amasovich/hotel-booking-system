package ru.mifi.booking.common.exception;

/**
 * 400 Bad Request — некорректные входные данные (кроме @Valid).
 */
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(400, message);
    }
}
