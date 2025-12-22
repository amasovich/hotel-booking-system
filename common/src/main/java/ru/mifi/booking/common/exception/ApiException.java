package ru.mifi.booking.common.exception;

/**
 * Базовое бизнес-исключение для REST API.
 *
 * <p>
 * Я храню HTTP-код здесь, чтобы сервис мог возвращать корректный статус (404/409/401 и т.д.),
 * а не превращать все ожидаемые ситуации в 500.
 * </p>
 */
public abstract class ApiException extends RuntimeException {

    private final int statusCode;

    protected ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * @return HTTP статус-код, который нужно вернуть клиенту.
     */
    public int getStatusCode() {
        return statusCode;
    }
}
