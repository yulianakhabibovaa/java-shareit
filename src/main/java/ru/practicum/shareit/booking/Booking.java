package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Booking {
    final LocalDate start;
    final LocalDate end;
    final long booker;
    final long item;
    final BookingStatus status;
}
