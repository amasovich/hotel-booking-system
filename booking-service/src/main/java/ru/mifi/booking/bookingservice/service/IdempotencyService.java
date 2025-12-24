package ru.mifi.booking.bookingservice.service;

import ru.mifi.booking.bookingservice.entity.RequestLog;
import ru.mifi.booking.common.exception.ConflictException;
import ru.mifi.booking.bookingservice.repository.RequestLogRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Идемпотентность через уникальный requestId.
 * Надёжность достигается не "exists() + save()", а уникальным индексом и обработкой гонки.
 */
@Service
public class IdempotencyService {

    private final RequestLogRepository requestLogRepository;

    public IdempotencyService(RequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    /**
     * Запоминаем requestId. Если такой requestId уже был — кидаем 409.
     */
    @Transactional
    public void rememberOrThrow(String requestId) {
        try {
            requestLogRepository.saveAndFlush(new RequestLog(requestId, OffsetDateTime.now()));
        } catch (DataIntegrityViolationException ex) {
            // Это и есть "правильная" идемпотентность: дубль детектится на уровне БД.
            throw new ConflictException("Duplicate request: X-Request-Id=" + requestId);
        }
    }
}

