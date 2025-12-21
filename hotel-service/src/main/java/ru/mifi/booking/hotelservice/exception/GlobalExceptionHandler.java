package ru.mifi.booking.hotelservice.exception;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.mifi.booking.common.dto.ErrorDto;
import ru.mifi.booking.common.http.RequestHeaders;

/**
 * Глобальный обработчик ошибок hotel-service.
 *
 * <p>
 * Здесь я привожу ошибки к единому формату ErrorDto.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка всех непредвиденных ошибок.
     *
     * @param ex исключение
     * @param request HTTP-запрос
     * @return ErrorDto в едином формате
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleAnyException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        String requestId = request.getHeader(RequestHeaders.X_REQUEST_ID);

        ErrorDto dto = new ErrorDto(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                requestId
        );

        return ResponseEntity.status(status).body(dto);
    }
}
