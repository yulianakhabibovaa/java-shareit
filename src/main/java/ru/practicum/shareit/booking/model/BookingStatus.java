package ru.practicum.shareit.booking.model;

public enum BookingStatus {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    APPROVED,
    REJECTED,
    CANCELED;

    public static BookingStatus valueOfOrNull(String status) {
        for (BookingStatus value : BookingStatus.values()) {
            if (value.name().equals(status)) {
                return value;
            }
        }
        return null;
    }
}
