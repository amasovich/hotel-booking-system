package ru.mifi.booking.hotelservice.service;

import ru.mifi.booking.hotelservice.dto.HotelDto;
import ru.mifi.booking.hotelservice.entity.Hotel;
import ru.mifi.booking.hotelservice.repository.HotelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    /**
     * Создать отель.
     */
    public HotelDto create(HotelDto dto) {
        Hotel saved = hotelRepository.save(new Hotel(null, dto.name(), dto.address()));
        return new HotelDto(saved.getId(), saved.getName(), saved.getAddress());
    }

    /**
     * Получить список отелей.
     */
    public List<HotelDto> list() {
        return hotelRepository.findAll().stream()
                .map(h -> new HotelDto(h.getId(), h.getName(), h.getAddress()))
                .toList();
    }

    /**
     * Получить отель или выбросить исключение (позже подключим красивый handler ошибок).
     */
    public Hotel getOrThrow(Long id) {
        return hotelRepository.findById(id).orElseThrow();
    }
}

