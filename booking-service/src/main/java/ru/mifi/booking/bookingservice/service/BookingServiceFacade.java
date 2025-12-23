package ru.mifi.booking.bookingservice.service;

import ru.mifi.booking.bookingservice.entity.Booking;
import ru.mifi.booking.bookingservice.entity.BookingStatus;
import ru.mifi.booking.bookingservice.dto.BookingDtos;
import ru.mifi.booking.common.exception.ConflictException;
import ru.mifi.booking.common.exception.NotFoundException;
import ru.mifi.booking.bookingservice.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mifi.booking.bookingservice.client.HotelServiceClient;
import ru.mifi.booking.bookingservice.client.dto.ConfirmAvailabilityRequest;
import ru.mifi.booking.bookingservice.security.JwtService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Бизнес-логика бронирований.
 * На Шаге 6: создаём PENDING, отменяем CANCELLED.
 * На Шаге 7: сюда добавится вызов hotel-service confirm-availability/release.
 */
@Service
public class BookingServiceFacade {

    private final BookingRepository bookingRepository;
    private final IdempotencyService idempotencyService;
    private final HotelServiceClient hotelServiceClient;
    private final JwtService jwtService;

    public BookingServiceFacade(
            BookingRepository bookingRepository,
            IdempotencyService idempotencyService,
            HotelServiceClient hotelServiceClient,
            JwtService jwtService
    ) {
        this.bookingRepository = bookingRepository;
        this.idempotencyService = idempotencyService;
        this.hotelServiceClient = hotelServiceClient;
        this.jwtService = jwtService;
    }

    public List<BookingDtos.BookingResponse> listByUser(Long userId) {
        return bookingRepository.findByUserId(userId).stream().map(this::toDto).toList();
    }

    public BookingDtos.BookingResponse get(Long id, Long userId) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));

        // “Человечно”: не палим чужие id — делаем вид, что не существует
        if (!b.getUserId().equals(userId)) {
            throw new NotFoundException("Booking " + id + " not found");
        }
        return toDto(b);
    }

    @Transactional
    public BookingDtos.BookingResponse create(Long userId, BookingDtos.CreateBookingRequest req, String requestId) {
        validateDates(req.startDate(), req.endDate());

        // 1) идемпотентность в рамках транзакции
        idempotencyService.rememberOrThrow(requestId);

        // 2) создаём бронь (пока PENDING)
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setRoomId(req.roomId());
        booking.setStartDate(req.startDate());
        booking.setEndDate(req.endDate());
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(OffsetDateTime.now());
        booking.setBookingUid(UUID.randomUUID().toString());

        Booking saved = bookingRepository.save(booking);

        // 3) вызываем hotel-service internal endpoint с service JWT
        String serviceJwt = jwtService.generateServiceToken();

        ConfirmAvailabilityRequest confirmReq = new ConfirmAvailabilityRequest(
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getBookingUid(),
                requestId
        );

        hotelServiceClient.confirmAvailability(saved.getRoomId(), confirmReq, serviceJwt, requestId);

        // 4) фиксируем статус
        saved.setStatus(BookingStatus.CONFIRMED);

        return toDto(saved);
    }

    @Transactional
    public void cancel(Long id, Long userId) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));

        if (!b.getUserId().equals(userId)) {
            throw new NotFoundException("Booking " + id + " not found");
        }

        if (b.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        // снять блокировку в hotel-service
        String serviceJwt = jwtService.generateServiceToken();
        hotelServiceClient.release(b.getRoomId(), b.getBookingUid(), serviceJwt, null);

        b.setStatus(BookingStatus.CANCELLED);
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new ConflictException("Dates are required");
        }
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ConflictException("Invalid dates: startDate must be before endDate");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new ConflictException("Invalid dates: startDate must be today or later");
        }
    }

    private BookingDtos.BookingResponse toDto(Booking b) {
        return new BookingDtos.BookingResponse(
                b.getId(),
                b.getBookingUid(),
                b.getRoomId(),
                b.getStartDate(),
                b.getEndDate(),
                b.getStatus()
        );
    }
}

