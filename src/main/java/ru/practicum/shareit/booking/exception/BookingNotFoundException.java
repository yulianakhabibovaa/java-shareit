package ru.practicum.shareit.booking.exception;

import ru.practicum.shareit.exception.NotFoundException;

public class BookingNotFoundException extends NotFoundException {
    public BookingNotFoundException(Long bookingId) {
        super("Бронирование с id " + bookingId + "не найдено");
    }

    public BookingNotFoundException(Long bookingId, Long bookerId) {
        super("Бронирование с id " + bookingId + "у пользователя" + bookerId + "не найдено");
    }

}
