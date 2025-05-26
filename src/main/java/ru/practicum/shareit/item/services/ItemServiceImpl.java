package ru.practicum.shareit.item.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.model.BookingProjection;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto addNewItem(ItemDto item, long ownerId) {
        User owner = getUser(ownerId);
        return ItemMapper.toItemDto(itemRepository.save(ItemMapper.toItem(item, owner)));
    }

    @Override
    @Transactional
    public ItemDto updateItem(long itemId, ItemDto update, long ownerId) {
        User owner = getUser(ownerId);
        Item item = getItem(itemId);
        if (!item.getOwner().equals(owner)) {
            throw new DataConflictException("Некорректный владелец предмета");
        }
        if (update.getName() != null) {
            item.setName(update.getName());
        }
        if (update.getDescription() != null) {
            item.setDescription(update.getDescription());
        }
        if (update.getAvailable() != null) {
            item.setAvailable(update.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public void delete(long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    public ItemDto findById(long itemId, long userId) {
        Item item = getItem(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(item);

        if (item.getOwner().getId().equals(userId)) {
            setLastAndNextBookings(itemDto);
        }
        List<CommentDto> comments = commentRepository.findByItem(item).stream()
                .map(CommentMapper::toCommentDto).toList();
        itemDto.setComments(comments);
        return itemDto;
    }

    private Item getItem(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ItemDto> findByOwner(long ownerId) {
        getUser(ownerId);
        userRepository.findById(ownerId);
        Collection<ItemDto> itemsDto = itemRepository.findByOwnerId(ownerId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
        for (ItemDto itemDto : itemsDto) {
            setLastAndNextBookings(itemDto);
            List<CommentDto> comments = commentRepository.findByItem(getItem(itemDto.getId())).stream()
                    .map(CommentMapper::toCommentDto).toList();
            itemDto.setComments(comments);
        }
        return itemsDto;
    }

    @Override
    public Collection<ItemDto> findBySearch(String text) {
        return itemRepository.findByNameOrDescription(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Transactional
    @Override
    public CommentDto createComment(long itemId, long userId, CommentRequestDto commentDto) {
        Item item = getItem(itemId);
        User user = getUser(userId);
        Booking booking = getBooking(user, item);
        if (!booking.getStatus().equals(APPROVED) || booking.getEnd().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Бронирование еще активно");
        }
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(user)
                .build();

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private void setLastAndNextBookings(ItemDto itemDto) {
        LocalDateTime now = LocalDateTime.now();
        BookingProjection bookings = bookingRepository.findNearestBookings(itemDto.getId(), now);
        Optional.ofNullable(bookings.getLastBooking())
                .ifPresent(booking -> itemDto.setLastBooking(BookingMapper.toBookingDto(booking)));
        Optional.ofNullable(bookings.getNextBooking())
                .ifPresent(booking -> itemDto.setNextBooking(BookingMapper.toBookingDto(booking)));
    }

    private User getUser(long ownerId) {
        return userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));
    }

    private Booking getBooking(User booker, Item item) {
        return bookingRepository.findByBookerAndItem(booker, item)
                .orElseThrow(() -> new BookingNotFoundException(item.getId(), booker.getId()));
    }
}
