package ru.mifi.booking.hotelservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mifi.booking.common.exception.ConflictException;
import ru.mifi.booking.common.exception.NotFoundException;
import ru.mifi.booking.hotelservice.dto.ConfirmAvailabilityRequest;
import ru.mifi.booking.hotelservice.dto.RoomDto;
import ru.mifi.booking.hotelservice.entity.Hotel;
import ru.mifi.booking.hotelservice.entity.Room;
import ru.mifi.booking.hotelservice.entity.RoomLock;
import ru.mifi.booking.hotelservice.repository.RoomLockRepository;
import ru.mifi.booking.hotelservice.repository.RoomRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomLockRepository roomLockRepository;

    public RoomService(RoomRepository roomRepository, RoomLockRepository roomLockRepository) {
        this.roomRepository = roomRepository;
        this.roomLockRepository = roomLockRepository;
    }

    /**
     * Добавить номер в отель.
     */
    public RoomDto addRoom(Hotel hotel, String number, boolean available) {
        Room room = new Room(null, hotel, number, available, 0);
        Room saved = roomRepository.save(room);
        return toDto(saved);
    }

    /**
     * Список доступных номеров на период.
     */
    public List<RoomDto> listAvailable(LocalDate start, LocalDate end) {
        return roomRepository.findAllAvailable().stream()
                .filter(r -> roomLockRepository.findOverlaps(r, start, end).isEmpty())
                .map(this::toDto)
                .toList();
    }

    /**
     * Рекомендованные номера: те же доступные, но отсортированы по timesBooked (по возрастанию).
     */
    public List<RoomDto> recommend(LocalDate start, LocalDate end) {
        return listAvailable(start, end).stream()
                .sorted(Comparator.comparingLong(RoomDto::timesBooked).thenComparing(RoomDto::id))
                .toList();
    }

    /**
     * INTERNAL: подтвердить доступность (временная блокировка).
     * Идемпотентность: если requestId уже был — просто выходим без ошибки.
     */
    @Transactional
    public void confirmAvailability(Long roomId, ConfirmAvailabilityRequest req) {
        if (roomLockRepository.findByRequestId(req.requestId()).isPresent()) {
            return; // повторный вызов с тем же requestId
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room " + roomId + " not found"));

        if (!room.isAvailable()) {
            throw new ConflictException("Room is not operational");
        }

        if (!roomLockRepository.findOverlaps(room, req.startDate(), req.endDate()).isEmpty()) {
            throw new ConflictException("Room is not available for this period");
        }

        RoomLock lock = new RoomLock(null, room, req.startDate(), req.endDate(), req.bookingId(), req.requestId());
        roomLockRepository.save(lock);

        // метрика справедливости
        room.setTimesBooked(room.getTimesBooked() + 1);
        // save не обязателен, если Room является managed-entity в текущей транзакции.
    }

    /**
     * INTERNAL: компенсирующее действие — снять блокировку.
     */
    @Transactional
    public void release(Long roomId, String bookingId) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room " + roomId + " not found"));

        roomLockRepository.findByBookingId(bookingId)
                .ifPresent(roomLockRepository::delete);
    }

    private RoomDto toDto(Room room) {
        return new RoomDto(
                room.getId(),
                room.getHotel().getId(),
                room.getNumber(),
                room.isAvailable(),
                room.getTimesBooked()
        );
    }
}
