package ru.mifi.booking.hotelservice.controller;

import ru.mifi.booking.hotelservice.dto.HotelDto;
import ru.mifi.booking.hotelservice.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    /**
     * ADMIN: создать отель.
     * (Роли подключим на следующем шаге — пока без @PreAuthorize)
     */
    @PostMapping
    public HotelDto create(@Valid @RequestBody HotelDto dto) {
        return hotelService.create(dto);
    }

    /**
     * USER: список отелей.
     */
    @GetMapping
    public List<HotelDto> list() {
        return hotelService.list();
    }
}
