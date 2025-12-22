package ru.mifi.booking.bookingservice.entity;

/**
 * Статусы жизненного цикла бронирования.
 * PENDING  -> создано, ожидает подтверждения (сагой).
 * CONFIRMED-> подтверждено (после блокировки/подтверждения в hotel-service).
 * CANCELLED-> отменено пользователем или сагой при ошибке.
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}

