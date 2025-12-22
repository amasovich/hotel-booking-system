package ru.mifi.booking.common.exception;

/**
 * 401 Unauthorized — нет прав/нет авторизации (если в проекте применяется).
 */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(401, message);
    }
}
