package ru.practicum.shareit.booking.model;

public interface BookingProjection {

    Booking getLastBooking();

    Booking getNextBooking();
}
