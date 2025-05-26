package ru.practicum.shareit.booking.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.model.BookingStatus.REJECTED;
import static ru.practicum.shareit.booking.model.BookingStatus.WAITING;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private static final Sort BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    public BookingResponseDto findById(long bookingId, long userId) {
        Booking booking = getBooking(bookingId);
        BookingResponseDto response;
        Item item = getItem(booking.getItem().getId());
        if (item.getOwner().getId().equals(userId) || booking.getBooker().getId().equals(userId)) {
            response = BookingMapper.toBookingResponseDto(booking);
        } else {
            throw new ValidationException("Бронь доступна только для владельца или забронировавшего");
        }
        return response;
    }

    @Override
    public List<BookingResponseDto> findByBookerId(long bookerId, String status) {
        getUser(bookerId);
        BookingStatus bookingStatus = BookingStatus.valueOfOrNull(status);
        if (bookingStatus == null) {
            throw new DataConflictException("Некорректный статус брони: " + status);
        }
        List<Booking> listBooking = switch (bookingStatus) {
            case ALL -> bookingRepository.findByBookerId(bookerId, BY_START_DESC);
            case CURRENT ->
                    bookingRepository.findByBookerIdAndEndAfterAndStartBefore(bookerId, LocalDateTime.now(), LocalDateTime.now(), BY_START_DESC);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(bookerId, LocalDateTime.now(), BY_START_DESC);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(bookerId, LocalDateTime.now(), BY_START_DESC);
            case WAITING, REJECTED, APPROVED ->
                    bookingRepository.findByBookerIdAndStatusEquals(bookerId, status, BY_START_DESC);
            default -> new ArrayList<>();
        };

        return listBooking.stream()
                .map(BookingMapper::toBookingResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> findByOwnerId(long ownerId, String status) {
        getUser(ownerId);
        BookingStatus bookingStatus = BookingStatus.valueOfOrNull(status);
        if (bookingStatus == null) {
            throw new DataConflictException("Некорректный статус брони: " + status);
        }

        List<Booking> listBooking = switch (bookingStatus) {
            case ALL -> bookingRepository.findByItemOwnerId(ownerId, BY_START_DESC);
            case CURRENT ->
                    bookingRepository.findByItemOwnerIdAndEndAfterAndStartBefore(ownerId, LocalDateTime.now(), LocalDateTime.now(), BY_START_DESC);
            case FUTURE ->
                    bookingRepository.findByItemOwnerIdAndStartAfter(ownerId, LocalDateTime.now(), BY_START_DESC);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(ownerId, LocalDateTime.now(), BY_START_DESC);
            case WAITING, REJECTED, APPROVED ->
                    bookingRepository.findByItemOwnerIdAndStatusEquals(ownerId, status, BY_START_DESC);
            default -> new ArrayList<>();
        };

        return listBooking.stream()
                .map(BookingMapper::toBookingResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public BookingResponseDto create(BookingDto bookingDto, long bookerId) {
        User booker = UserMapper.toUser(userService.findById(bookerId));
        bookingDto.setBookerId(booker.getId());
        Item item = getItem(bookingDto.getItemId());

        if (!item.getAvailable()) {
            throw new ValidationException("Предмет недоступен");
        }
        if (bookingRepository.isAvailable(item.getId(), bookingDto.getStart(), bookingDto.getEnd())) {
            throw new ValidationException("Предмет на эти даты недоступен");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ValidationException("Владелец не может создать бронь");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования не может быть раньше даты начала");
        }

        bookingDto.setBookerId(booker.getId());
        Booking booking = Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(WAITING)
                .build();
        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approve(long bookingId, boolean approved, long ownerId) {
        Booking booking = getBooking(bookingId);
        Item item = booking.getItem();

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ValidationException("Подтвердить бронь может только владелец");
        }
        if (booking.getStatus().equals(WAITING)) {
            booking.setStatus(approved ? APPROVED : REJECTED);
        } else {
            throw new ValidationException("Нельзя подтвердить бронирование в статусе " + booking.getStatus());
        }
        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    private Booking getBooking(long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    private Item getItem(long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    private User getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
