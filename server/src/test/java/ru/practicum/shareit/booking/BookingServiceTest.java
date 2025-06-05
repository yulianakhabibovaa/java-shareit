package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.services.BookingService;
import ru.practicum.shareit.exception.DataConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
class BookingServiceTest {
    @Autowired
    private BookingService service;

    private static final Long BOOKING_ID_1 = 1L;
    private static final Long BOOKING_ID_2 = 2L;
    private static final Long ITEM_ID_1 = 1L;
    private static final Long ITEM_ID_2 = 2L;
    private static final Long ITEM_ID_3 = 3L;
    private static final Long USER_ID_1 = 1L;
    private static final Long USER_ID_2 = 2L;
    private static final Long USER_ID_3 = 3L;

    private ItemDto unavailableItem;
    private ItemDto availableItem1;
    private ItemDto availableItem2;
    private UserDto booker;
    private UserDto owner;

    @BeforeEach
    void setUp() {
        unavailableItem = new ItemDto(ITEM_ID_1, "Ручка ", "Ручка шариковая", false, null, null, null, null);
        availableItem1 = new ItemDto(ITEM_ID_2, "Карандаш", "Карандаш грифельный", true, null, null, null, null);
        availableItem2 = new ItemDto(ITEM_ID_3, "Тетрадь", "Тетрадь в клеточку", true, null, null, null, null);
        booker = new UserDto(USER_ID_1, "Первый", "user1@mail.ru");
        owner = new UserDto(USER_ID_2, "Второй", "user2@mail.ru");
    }

    private BookingResponseDto createBookingResponseDto() {
        return BookingResponseDto.builder()
                .id(BOOKING_ID_1)
                .booker(booker)
                .item(availableItem1)
                .status(BookingStatus.WAITING)
                .build();
    }

    private BookingDto createTestBookingDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        return BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
    }

    @Test
    void findById_WhenUserIsOwner_ShouldReturnBooking() {
        BookingResponseDto result = service.findById(BOOKING_ID_1, USER_ID_2);

        assertEquals(createBookingResponseDto().getItem(), result.getItem());
        assertNotEquals(USER_ID_2, result.getBooker());
        assertEquals(createBookingResponseDto().getStatus(), result.getStatus());
    }

    @Test
    void findById_WhenUserIsBooker_ShouldReturnBooking() {
        BookingResponseDto result = service.findById(BOOKING_ID_1, USER_ID_1);

        assertEquals(createBookingResponseDto().getStatus(), result.getStatus());
        assertEquals(USER_ID_1, result.getBooker().getId());
        assertEquals(createBookingResponseDto().getItem(), result.getItem());
    }

    @Test
    void findById_WhenUserNotAuthorized_ShouldThrowException() {
        assertThrows(ValidationException.class,
                () -> service.findById(BOOKING_ID_1, USER_ID_3));
    }

    @Test
    void createBooking_WhenItemAvailable_ShouldCreateBooking() {
        LocalDateTime now = LocalDateTime.now();
        BookingDto bookingDto = createTestBookingDto(ITEM_ID_3, now, now.plusDays(1));

        BookingResponseDto result = service.create(bookingDto, USER_ID_1);

        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals(USER_ID_1, result.getBooker().getId());
        assertEquals(ITEM_ID_3, result.getItem().getId());
    }

    @Test
    void createBooking_WhenItemUnavailable_ShouldThrowException() {
        BookingDto bookingDto = createTestBookingDto(ITEM_ID_1, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class,
                () -> service.create(bookingDto, USER_ID_1));
    }

    @Test
    void createBooking_WhenOwnerBooksOwnItem_ShouldThrowException() {
        BookingDto bookingDto = createTestBookingDto(ITEM_ID_2, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class,
                () -> service.create(bookingDto, USER_ID_2));
    }

    @Test
    void approve_WhenOwnerApproves_ShouldUpdateStatus() {
        BookingResponseDto result = service.approve(BOOKING_ID_1, true, USER_ID_2);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void approve_WhenOwnerRejects_ShouldUpdateStatus() {

        BookingResponseDto result = service.approve(BOOKING_ID_1, false, USER_ID_2);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void approve_WhenStatusNotWaiting_ShouldThrowException() {

        assertThrows(ValidationException.class,
                () -> service.approve(BOOKING_ID_2, true, USER_ID_1));
    }

    @Test
    void findByBookerId_WithAllStatus_ShouldReturnAllBookings() {
        List<BookingResponseDto> result = service.findByBookerId(USER_ID_1, "ALL");

        assertEquals(1, result.size());
        assertEquals(BOOKING_ID_1, result.get(0).getId());
    }

    @Test
    void findByBookerId_WithInvalidStatus_ShouldThrowException() {
        assertThrows(DataConflictException.class,
                () -> service.findByBookerId(USER_ID_1, "INVALID_STATUS"));
    }
}