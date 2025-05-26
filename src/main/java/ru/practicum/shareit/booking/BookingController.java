package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.services.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public BookingResponseDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                     @RequestBody @Valid BookingDto bookingDto) {
        log.info("Создаем бронь: {}", bookingDto);
        BookingResponseDto response = bookingService.create(bookingDto, userId);
        log.info("Бронирование создано: {}", response);
        return response;
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approve(@PathVariable(name = "bookingId") long bookingId,
                                    @RequestParam(value = "approved") boolean approved,
                                    @RequestHeader(X_SHARER_USER_ID) long ownerId) {
        log.info("Подтверждение брони владельцем: {}", ownerId);
        BookingResponseDto bookingDto = bookingService.approve(bookingId, approved, ownerId);
        log.info("Бронирование c id {} {}", bookingId, approved ? "подтверждено" : "отклонено");
        return bookingDto;
    }


    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@PathVariable(name = "bookingId") long bookingId,
                                       @RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Получаем данные о бронировании {}", bookingId);
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookings(@RequestParam(value = "state", defaultValue = "ALL") String status,
                                                @RequestHeader(X_SHARER_USER_ID) long bookerId) {
        log.info("Получаем бронирования пользователя {}", bookerId);
        return bookingService.findByBookerId(bookerId, status);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(@RequestParam(value = "state", defaultValue = "ALL") String status,
                                                   @RequestHeader(X_SHARER_USER_ID) long ownerId) {
        log.info("Получаем бронирования по владельцу {}", ownerId);
        return bookingService.findByOwnerId(ownerId, status);
    }

}
