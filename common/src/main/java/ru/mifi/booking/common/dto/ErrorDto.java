package ru.mifi.booking.common.dto;

import java.time.Instant;

/**
 * Единый формат ошибки для REST API.
 *
 * <p>
 * Этот DTO я возвращаю из всех сервисов в одинаковом формате,
 * чтобы клиент (и проверяющий) всегда понимал структуру ошибок.
 * </p>
 */
public class ErrorDto {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String requestId;

    /**
     * Пустой конструктор нужен для сериализации/десериализации.
     */
    public ErrorDto() {
    }

    /**
     * Удобный конструктор для сборки ответа об ошибке.
     *
     * @param timestamp время ошибки
     * @param status HTTP-статус
     * @param error краткое имя ошибки
     * @param message сообщение ошибки
     * @param path путь запроса
     * @param requestId requestId из заголовка X-Request-Id
     */
    public ErrorDto(Instant timestamp, int status, String error, String message, String path, String requestId) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.requestId = requestId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
