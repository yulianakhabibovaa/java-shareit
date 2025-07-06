package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
    @NotNull(message = "id предмета не может быть пустым")
    private long itemId;
    @FutureOrPresent(message = "Некорректная дата начала бронирования")
    private LocalDateTime start;
    @Future(message = "Некорректная дата окончания бронирования")
    private LocalDateTime end;

    @AssertTrue(message = "Дата начала должна быть раньше даты окончания")
    private boolean isValidDateRange() {
        if (start == null || end == null) {
            return true;
        }
        return start.isBefore(end);
    }
}