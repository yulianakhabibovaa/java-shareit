package ru.practicum.shareit.booking.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor
public class BookingDto {
    private final LocalDate start;
    private final LocalDate end;
    private final long item;
    private final BookingStatus status;
}
