package ru.practicum.shareit.booking;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor
public class Booking {
    private final LocalDate start;
    private final LocalDate end;
    private final long booker;
    private final long item;
    private final BookingStatus status;
}
