package ru.mifi.booking.bookingservice.controller;

import ru.mifi.booking.bookingservice.dto.BookingDtos;
import ru.mifi.booking.common.exception.UnauthorizedException;
import ru.mifi.booking.bookingservice.service.BookingServiceFacade;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * API бронирований.
 * Важно: userId берём из Authentication. Если у вас JWT уже готов — будет работать “как надо”.
 * Если Security ещё не подключали — временно можно подставить userId=1 и убрать Authentication.
 */
@RestController
public class BookingController {

    private final BookingServiceFacade bookingService;

    public BookingController(BookingServiceFacade bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/booking")
    public BookingDtos.BookingResponse create(@Valid @RequestBody BookingDtos.CreateBookingRequest req,
                                              Authentication auth,
                                              @RequestHeader(name = "X-Request-Id", required = false) String requestId) {

        if (auth == null) {
            throw new UnauthorizedException("No auth");
        }

        // “человечно”: пока без реальной таблицы users можно привязаться к имени пользователя
        Long userId = Math.abs(auth.getName().hashCode()) * 1L;

        if (requestId == null || requestId.isBlank()) {
            // если Gateway не поставил — генерируем, но тогда идемпотентность будет “одноразовая”
            requestId = UUID.randomUUID().toString();
        }

        return bookingService.create(userId, req, requestId);
    }

    @GetMapping("/bookings")
    public List<BookingDtos.BookingResponse> list(Authentication auth) {
        if (auth == null) throw new UnauthorizedException("No auth");
        Long userId = Math.abs(auth.getName().hashCode()) * 1L;
        return bookingService.listByUser(userId);
    }

    @GetMapping("/booking/{id}")
    public BookingDtos.BookingResponse get(@PathVariable Long id, Authentication auth) {
        if (auth == null) throw new UnauthorizedException("No auth");
        Long userId = Math.abs(auth.getName().hashCode()) * 1L;
        return bookingService.get(id, userId);
    }

    @DeleteMapping("/booking/{id}")
    public void cancel(@PathVariable Long id,
                       Authentication auth,
                       @RequestHeader(name = "X-Request-Id", required = false) String requestId) {

        if (auth == null) throw new UnauthorizedException("No auth");

        // requestId для cancel можно тоже логировать отдельной таблицей — но по плану достаточно для POST
        if (requestId == null || requestId.isBlank()) requestId = UUID.randomUUID().toString();

        Long userId = Math.abs(auth.getName().hashCode()) * 1L;
        bookingService.cancel(id, userId);
    }
}

