package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.DataConflictException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.services.ItemServiceImpl;
import ru.practicum.shareit.request.dto.RequestedItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.shareit.request.mapper.ItemRequestMapper.toRequestedItemDto;

@Transactional
@SpringBootTest
class ItemServiceTest {

    @Autowired
    private ItemServiceImpl service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder().name("Owner").email("owner@example.com").build());
        booker = userRepository.save(User.builder().name("Booker").email("booker@example.com").build());
        item = itemRepository.save(
                Item.builder().name("Компьютер").description("Два ядра два гига").available(true).owner(owner).build()
        );

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);
    }

    @AfterEach
    void tearDown() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE comments RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE bookings RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE items RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    private ItemDto createTestItemDto(Long id, String name, String description, boolean available) {
        return ItemDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .build();
    }

    private CommentRequestDto createTestCommentRequestDto(String text) {
        return CommentRequestDto.builder()
                .text(text)
                .build();
    }

    @Test
    void createItem_WhenValidData_ShouldCreateItem() {
        ItemDto newItem = createTestItemDto(null, "Фломастеры", "Для раскрасок", true);

        Long itemId = service.addNewItem(newItem, owner.getId()).getId();

        Item createdItem = itemRepository.findById(itemId).orElseThrow();
        assertNotNull(createdItem.getId());
        assertEquals(newItem.getName(), createdItem.getName());
        assertEquals(newItem.getDescription(), createdItem.getDescription());
        assertEquals(newItem.getAvailable(), createdItem.getAvailable());
    }

    @Test
    void createItem_WhenUserNotExist_ShouldThrowException() {
        ItemDto newItem = createTestItemDto(null, "Скотч", "Заматывать штуки", true);
        long invalidUserId = 999L;

        assertThrows(UserNotFoundException.class,
                () -> service.addNewItem(newItem, invalidUserId));
    }

    @Test
    void updateItem_WhenValidOwner_ShouldUpdateItem() {
        ItemDto updateData = createTestItemDto(null, "Пассатижи", "Здоровенные", true);
        service.updateItem(item.getId(), updateData, owner.getId());

        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(updateData.getName(), updatedItem.getName());
        assertEquals(updateData.getDescription(), updatedItem.getDescription());
    }

    @Test
    void updateItem_WhenInvalidOwner_ShouldThrowException() {
        ItemDto updateData = createTestItemDto(item.getId(), "Пила", "Жужужу", true);

        assertThrows(DataConflictException.class,
                () -> service.updateItem(item.getId(), updateData, booker.getId()));
    }

    @Test
    void deleteItem_WhenExists_ShouldRemoveItem() {
        service.delete(item.getId());

        assertThrows(ItemNotFoundException.class,
                () -> service.findById(item.getId(), owner.getId()));
        assertTrue(itemRepository.findById(item.getId()).isEmpty());
    }

    @Test
    void findById_WhenExists_ShouldReturnItem() {
        ItemDto foundItem = service.findById(item.getId(), owner.getId());

        assertEquals(item.getId(), foundItem.getId());
        assertEquals(item.getName(), foundItem.getName());
        assertEquals(item.getDescription(), foundItem.getDescription());
    }

    @Test
    void findByOwner_ShouldReturnOwnerItems() {
        Collection<ItemDto> ownerItems = service.findByOwner(owner.getId());

        assertEquals(1, ownerItems.size());
        assertTrue(ownerItems.stream().anyMatch(it -> it.getId().equals(item.getId())));
        assertTrue(ownerItems.stream().anyMatch(it -> it.getName().equals("Компьютер")));
        assertTrue(ownerItems.stream().anyMatch(it -> it.getDescription().equals("Два ядра два гига")));
    }

    @Test
    void createComment_WhenValidBooking_ShouldCreateComment() {
        CommentRequestDto commentDto = createTestCommentRequestDto("Суперские инструменты!");
        Long commentId = service.createComment(item.getId(), booker.getId(), commentDto).getId();

        List<Comment> comments = commentRepository.findByItem(item);
        assertEquals(1, comments.size());
        Comment comment = comments.getFirst();
        assertEquals(commentDto.getText(), comment.getText());
        assertNotNull(comment.getId());
        assertEquals(commentId, comment.getId());
        assertNotNull(comment.getCreated());
    }

    @Test
    void createComment_WhenNoBooking_ShouldThrowException() {
        CommentRequestDto commentDto = createTestCommentRequestDto("Отличная вещь!");

        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("new@example.com");
        userRepository.save(newUser);

        assertThrows(BookingNotFoundException.class,
                () -> service.createComment(item.getId(), newUser.getId(), commentDto));
    }

    @Test
    void findBySearch_ShouldReturnMatchingItems() {
        ItemDto newItem = createTestItemDto(null, "Оператива гиг", "Для старого игрового компьютера", true);
        service.addNewItem(newItem, owner.getId());
        String searchText = "гиг";
        Collection<ItemDto> searchResults = service.findBySearch(searchText);

        assertEquals(2, searchResults.size());
        assertTrue(searchResults.stream().allMatch(it ->
                it.getName().contains(searchText) || it.getDescription().contains(searchText)));
    }

    @Test
    void findBySearch_WhenBlankText_ShouldReturnEmptyList() {
        Collection<ItemDto> searchResults = service.findBySearch("   ");
        assertTrue(searchResults.isEmpty());
    }

    @Test
    void toItemForRequestDto_ShouldConvertCorrectly() {
        RequestedItemDto requested = toRequestedItemDto(item);

        assertEquals(item.getId(), requested.getId());
        assertEquals(item.getName(), requested.getName());
        assertEquals(item.getOwner().getId(), requested.getOwnerId());
    }
}
