package ru.mifi.booking.bookingservice.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.mifi.booking.common.dto.ErrorDto;
import ru.mifi.booking.common.exception.ApiException;
import ru.mifi.booking.common.http.RequestHeaders;

/**
 * Глобальный обработчик ошибок booking-service.
 *
 * <p>
 * Я привожу все ошибки к единому формату {@link ErrorDto} из модуля common,
 * сохраняя единообразие ответа для API Gateway и клиентов.
 * </p>
 *
 * <p>
 * При этом я возвращаю корректные HTTP-статусы:
 * <ul>
 *     <li>{@link ApiException} → 404/409/... в зависимости от исключения</li>
 *     <li>{@link MethodArgumentNotValidException} → 400 Bad Request</li>
 *     <li>любая другая ошибка → 500 Internal Server Error</li>
 * </ul>
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MDC_REQUEST_ID_KEY = "requestId";

    /**
     * Обработка ожидаемых бизнес-ошибок (404/409 и т.д.).
     *
     * @param ex      бизнес-исключение
     * @param request HTTP-запрос
     * @return ErrorDto в едином формате
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorDto> handleApiException(ApiException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorDto dto = buildDto(status, ex.getMessage(), request);
        return ResponseEntity.status(status).body(dto);
    }

    /**
     * Ошибки валидации DTO (например, @Valid / @NotNull и т.п.).
     *
     * @param ex      исключение валидации
     * @param request HTTP-запрос
     * @return ErrorDto в едином формате
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationException(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (message == null || message.isBlank()) {
            message = "Validation error";
        }

        ErrorDto dto = buildDto(status, message, request);
        return ResponseEntity.status(status).body(dto);
    }

    /**
     * Обработка всех непредвиденных ошибок (фолбэк).
     *
     * @param ex      исключение
     * @param request HTTP-запрос
     * @return ErrorDto в едином формате
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleAnyException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorDto dto = buildDto(status, ex.getMessage(), request);
        return ResponseEntity.status(status).body(dto);
    }

    private ErrorDto buildDto(HttpStatus status, String message, HttpServletRequest request) {
        String requestId = resolveRequestId(request);

        return new ErrorDto(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                requestId
        );
    }

    /**
     * Получение requestId в приоритетном порядке:
     * 1) из заголовка X-Request-Id
     * 2) из request attribute (если фильтр положил туда значение)
     * 3) из MDC (на случай, если ошибка случилась после установки MDC)
     */
    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(RequestHeaders.X_REQUEST_ID);
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }

        Object attr = request.getAttribute(RequestHeaders.X_REQUEST_ID);
        if (attr != null) {
            String attrValue = String.valueOf(attr);
            if (!attrValue.isBlank()) {
                return attrValue;
            }
        }

        String mdcValue = MDC.get(MDC_REQUEST_ID_KEY);
        if (mdcValue != null && !mdcValue.isBlank()) {
            return mdcValue;
        }

        return null;
    }
}
