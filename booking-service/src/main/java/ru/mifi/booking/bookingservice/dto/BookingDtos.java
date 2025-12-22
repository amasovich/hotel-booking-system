package ru.mifi.booking.bookingservice.dto;

import ru.mifi.booking.bookingservice.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO для API бронирований.
 * На Шаге 7 сюда можно добавить autoSelect, если он у вас предусмотрен планом.
 */
public final class BookingDtos {

    private BookingDtos() {
        // утилитный класс
    }

    public record CreateBookingRequest(
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @NotNull Long roomId
    ) {}

    public record BookingResponse(
            Long id,
            String bookingUid,
            Long roomId,
            LocalDate startDate,
            LocalDate endDate,
            BookingStatus status
    ) {}
}

