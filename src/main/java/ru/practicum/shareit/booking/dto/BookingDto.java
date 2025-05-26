package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class BookingDto {
    private Long id;
    @NotNull
    LocalDateTime start;
    @NotNull
    LocalDateTime end;
    @NotNull
    Long itemId;
    Long bookerId;
    BookingStatus status;
}
