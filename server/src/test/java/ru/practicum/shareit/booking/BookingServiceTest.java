package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.services.BookingService;
import ru.practicum.shareit.exception.BusinessException;
import ru.practicum.shareit.exception.DataConflictException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
class BookingServiceTest {
    @Autowired
    private BookingService service;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EntityManager entityManager;

    private User owner;
    private User booker;
    private Item availableItem1;
    private Item availableItem2;
    private Item unavailableItem;
    private Booking booking1;
    private Booking booking2;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());

        unavailableItem = itemRepository.save(Item.builder()
                .name("Ручка")
                .description("Ручка шариковая")
                .available(false)
                .owner(owner)
                .build());

        availableItem1 = itemRepository.save(Item.builder()
                .name("Карандаш")
                .description("Карандаш грифельный")
                .available(true)
                .owner(owner)
                .build());

        availableItem2 = itemRepository.save(Item.builder()
                .name("Тетрадь")
                .description("Тетрадь в клеточку")
                .available(true)
                .owner(owner)
                .build());

        booking1 = bookingRepository.save(Booking.builder()
                .item(availableItem1)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.WAITING)
                .build());

        booking2 = bookingRepository.save(Booking.builder()
                .item(availableItem2)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .build());
    }

    @AfterEach
    void tearDown() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE bookings").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE items").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE users").executeUpdate();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    private BookingDto createTestBookingDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        return BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    private ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    @Test
    void findById_WhenUserIsOwner_ShouldReturnBooking() {
        BookingResponseDto result = service.findById(booking1.getId(), owner.getId());

        assertEquals(booking1.getId(), result.getId());
        assertEquals(toUserDto(booker), result.getBooker());
        assertEquals(toItemDto(availableItem1), result.getItem());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void findById_WhenUserIsBooker_ShouldReturnBooking() {
        BookingResponseDto result = service.findById(booking1.getId(), booker.getId());

        assertEquals(booking1.getId(), result.getId());
        assertEquals(toUserDto(booker), result.getBooker());
        assertEquals(toItemDto(availableItem1), result.getItem());
        assertEquals(BookingStatus.WAITING, result.getStatus());
    }

    @Test
    void findById_WhenUserNotAuthorized_ShouldThrowException() {
        User unauthorizedUser = userRepository.save(User.builder()
                .name("Unauthorized")
                .email("unauth@example.com")
                .build());

        assertThrows(BusinessException.class,
                () -> service.findById(booking1.getId(), unauthorizedUser.getId()));
    }

    @Test
    void createBooking_WhenItemAvailable_ShouldCreateBooking() {
        LocalDateTime now = LocalDateTime.now();
        BookingDto bookingDto = createTestBookingDto(availableItem1.getId(), now, now.plusDays(1));

        BookingResponseDto result = service.create(bookingDto, booker.getId());

        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(availableItem1.getId(), result.getItem().getId());

        Booking savedBooking = bookingRepository.findById(result.getId()).orElseThrow();
        assertEquals(BookingStatus.WAITING, savedBooking.getStatus());
        assertEquals(booker.getId(), savedBooking.getBooker().getId());
        assertEquals(availableItem1.getId(), savedBooking.getItem().getId());
    }

    @Test
    void createBooking_WhenItemUnavailable_ShouldThrowException() {
        BookingDto bookingDto = createTestBookingDto(
                unavailableItem.getId(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(BusinessException.class,
                () -> service.create(bookingDto, booker.getId()));
    }

    @Test
    void createBooking_WhenOwnerBooksOwnItem_ShouldThrowException() {
        BookingDto bookingDto = createTestBookingDto(
                availableItem1.getId(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(BusinessException.class,
                () -> service.create(bookingDto, owner.getId()));
    }

    @Test
    void approve_WhenOwnerApproves_ShouldUpdateStatus() {
        BookingResponseDto result = service.approve(booking1.getId(), true, owner.getId());

        assertEquals(BookingStatus.APPROVED, result.getStatus());

        Booking updatedBooking = bookingRepository.findById(booking1.getId()).orElseThrow();
        assertEquals(BookingStatus.APPROVED, updatedBooking.getStatus());
    }

    @Test
    void approve_WhenOwnerRejects_ShouldUpdateStatus() {
        BookingResponseDto result = service.approve(booking1.getId(), false, owner.getId());

        assertEquals(BookingStatus.REJECTED, result.getStatus());

        Booking updatedBooking = bookingRepository.findById(booking1.getId()).orElseThrow();
        assertEquals(BookingStatus.REJECTED, updatedBooking.getStatus());
    }

    @Test
    void approve_WhenStatusNotWaiting_ShouldThrowException() {
        assertThrows(BusinessException.class,
                () -> service.approve(booking2.getId(), true, owner.getId()));
    }

    @Test
    void findByBookerId_WithAllStatus_ShouldReturnAllBookings() {
        List<BookingResponseDto> result = service.findByBookerId(booker.getId(), "ALL");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getId().equals(booking1.getId())));
        assertTrue(result.stream().anyMatch(b -> b.getId().equals(booking2.getId())));
    }

    @Test
    void findByBookerId_WithInvalidStatus_ShouldThrowException() {
        assertThrows(DataConflictException.class,
                () -> service.findByBookerId(booker.getId(), "INVALID_STATUS"));
    }

    @Test
    void findByOwnerId_WhenUserNotFound_ShouldThrowException() {
        assertThrows(UserNotFoundException.class,
                () -> service.findByOwnerId(999L, "ALL"));
    }

    @Test
    void findByOwnerId_WithInvalidStatus_ShouldThrowException() {
        assertThrows(DataConflictException.class,
                () -> service.findByOwnerId(owner.getId(), "INVALID_STATUS"));
    }

    @Test
    void findByOwnerId_WithAllStatus_ShouldReturnAllBookingsForOwner() {
        List<BookingResponseDto> result = service.findByOwnerId(owner.getId(), "ALL");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(b -> b.getId().equals(booking1.getId())));
        assertTrue(result.stream().anyMatch(b -> b.getId().equals(booking2.getId())));
    }

    @Test
    void findByOwnerId_WithCurrentStatus_ShouldReturnCurrentBookings() {
        Booking currentBooking = bookingRepository.save(Booking.builder()
                .item(availableItem1)
                .booker(booker)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .status(BookingStatus.APPROVED)
                .build());

        List<BookingResponseDto> result = service.findByOwnerId(owner.getId(), "CURRENT");

        assertEquals(1, result.size());
        assertEquals(currentBooking.getId(), result.get(0).getId());
    }

    @Test
    void findByOwnerId_WithFutureStatus_ShouldReturnFutureBookings() {
        List<BookingResponseDto> result = service.findByOwnerId(owner.getId(), "FUTURE");

        assertEquals(1, result.size());
        assertEquals(booking2.getId(), result.get(0).getId());
    }

    @Test
    void findByOwnerId_WithPastStatus_ShouldReturnPastBookings() {
        List<BookingResponseDto> result = service.findByOwnerId(owner.getId(), "PAST");

        assertEquals(1, result.size());
        assertEquals(booking1.getId(), result.get(0).getId());
    }

    @Test
    void findByOwnerId_WithWaitingStatus_ShouldReturnWaitingBookings() {
        List<BookingResponseDto> result = service.findByOwnerId(owner.getId(), "WAITING");

        assertEquals(1, result.size());
        assertEquals(booking1.getId(), result.get(0).getId());
    }

    @Test
    void findByOwnerId_WithRejectedStatus_ShouldReturnRejectedBookings() {
        service.approve(booking1.getId(), false, owner.getId());

        List<BookingResponseDto> result = service.findByOwnerId(owner.getId(), "REJECTED");

        assertEquals(1, result.size());
        assertEquals(booking1.getId(), result.get(0).getId());
        assertEquals(BookingStatus.REJECTED, result.get(0).getStatus());
    }

    @Test
    void findByOwnerId_WithApprovedStatus_ShouldReturnApprovedBookings() {
        List<BookingResponseDto> result = service.findByOwnerId(owner.getId(), "APPROVED");

        assertEquals(1, result.size());
        assertEquals(booking2.getId(), result.get(0).getId());
        assertEquals(BookingStatus.APPROVED, result.get(0).getStatus());
    }
}