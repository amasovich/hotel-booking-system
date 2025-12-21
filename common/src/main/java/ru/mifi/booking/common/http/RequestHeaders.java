package ru.mifi.booking.common.http;

/**
 * Общие HTTP-заголовки, используемые всеми сервисами системы.
 *
 * <p>
 * Я держу эти константы в common-модуле, чтобы:
 * 1) не было опечаток,
 * 2) все сервисы использовали одинаковые имена заголовков.
 * </p>
 */
public final class RequestHeaders {

    /**
     * Заголовок сквозной трассировки запроса.
     */
    public static final String X_REQUEST_ID = "X-Request-Id";

    private RequestHeaders() {
        // Утилитный класс: создание экземпляров не требуется.
    }
}
