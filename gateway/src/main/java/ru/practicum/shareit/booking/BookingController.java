package ru.practicum.shareit.booking;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingClient bookingClient;
    static final String X_SHARED_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(X_SHARED_USER_ID) long userId,
                                                @RequestBody BookItemRequestDto bookingDto) {
        return bookingClient.bookItem(userId, bookingDto);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader(X_SHARED_USER_ID) long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new ValidationException("Неизвестное состояние брони"));
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnedBookings(@RequestHeader(X_SHARED_USER_ID) long userId) {
        return bookingClient.getOwnedBookings(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getBooking(@RequestHeader(X_SHARED_USER_ID) long userId,
                                             @PathVariable long id) {
        return bookingClient.getBooking(userId, id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> approve(@RequestHeader(X_SHARED_USER_ID) long ownerId,
                                          @PathVariable long id,
                                          @RequestParam boolean approved) {
        return bookingClient.approve(id, approved, ownerId);
    }
}