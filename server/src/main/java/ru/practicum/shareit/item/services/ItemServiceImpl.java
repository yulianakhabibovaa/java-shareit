package ru.practicum.shareit.item.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BusinessException;
import ru.practicum.shareit.exception.DataConflictException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto addNewItem(ItemDto item, long ownerId) {
        User owner = getUser(ownerId);
        Item saved = ItemMapper.toItem(item, owner);
        if (item.getRequestId() != null) {
            saved.setRequest(itemRequestRepository.findById(item.getRequestId())
                    .orElseThrow(() -> new ItemRequestNotFoundException(item.getRequestId())));
        }
        return ItemMapper.toItemDto(itemRepository.save(saved));
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
    @Transactional
    public void delete(long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Collection<ItemDto> findBySearch(String text) {
        return itemRepository.findByNameOrDescription(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto createComment(long itemId, long userId, CommentRequestDto commentDto) {
        Item item = getItem(itemId);
        User user = getUser(userId);
        Booking booking = getBooking(user, item);
        LocalDateTime now = LocalDateTime.now();
        if (!booking.getStatus().equals(BookingStatus.APPROVED) || booking.getEnd().isAfter(now)) {
            throw new BusinessException("Бронирование еще активно");
        }

        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDto, item, user)));
    }

    private void setLastAndNextBookings(ItemDto itemDto) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> last = bookingRepository.findLastBooking(
                itemDto.getId(),
                now,
                PageRequest.of(0, 1)
        );
        List<Booking> next = bookingRepository.findNextBooking(
                itemDto.getId(),
                now,
                PageRequest.of(0, 1)
        );
        itemDto.setLastBooking(last.isEmpty() ? null : BookingMapper.toBookingDto(last.getFirst()));
        itemDto.setNextBooking(next.isEmpty() ? null : BookingMapper.toBookingDto(next.getFirst()));
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Booking getBooking(User booker, Item item) {
        return bookingRepository.findFirstByBookerAndItemOrderByStartDesc(booker, item)
                .orElseThrow(() -> new BookingNotFoundException(item.getId(), booker.getId()));
    }

    private Item getItem(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }
}
