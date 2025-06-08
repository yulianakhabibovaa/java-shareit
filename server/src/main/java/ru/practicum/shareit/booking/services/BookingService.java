package ru.practicum.shareit.booking.services;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

@Service
public interface BookingService {
    BookingResponseDto create(BookingDto bookingDto, long id);

    BookingResponseDto approve(long bookingId, boolean approved, long ownerId);

    BookingResponseDto findById(long bookingId, long userId);

    List<BookingResponseDto> findByBookerId(long bookerId, String status);

    List<BookingResponseDto> findByOwnerId(long ownerId, String status);
}
