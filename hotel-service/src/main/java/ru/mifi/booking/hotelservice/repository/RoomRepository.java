package ru.mifi.booking.hotelservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mifi.booking.hotelservice.entity.Room;
import ru.mifi.booking.hotelservice.entity.RoomLock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Все номера, которые "в принципе доступны" (не выведены из эксплуатации).
     */
    @Query("select r from Room r where r.available = true")
    List<Room> findAllAvailable();
}

