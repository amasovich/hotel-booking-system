package ru.mifi.booking.common.exception;

/**
 * 409 Conflict — конфликт состояния (дубликат X-Request-Id, ресурс занят и т.д.).
 */
public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(409, message);
    }
}
