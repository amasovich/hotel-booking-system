package ru.mifi.booking.bookingservice.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Таблица для идемпотентности.
 * Повторный X-Request-Id не должен повторно обрабатывать операцию создания.
 */
@Entity
@Table(
        name = "request_log",
        indexes = { @Index(name = "idx_request_id", columnList = "requestId", unique = true) }
)
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Идемпотентный ключ (заголовок X-Request-Id). */
    @Column(nullable = false, unique = true)
    private String requestId;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    public RequestLog() {
        // JPA
    }

    public RequestLog(String requestId, OffsetDateTime createdAt) {
        this.requestId = requestId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getRequestId() { return requestId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
