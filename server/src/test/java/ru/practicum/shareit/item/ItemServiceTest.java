package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.DataConflictException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.services.ItemServiceImpl;
import ru.practicum.shareit.request.dto.RequestedItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.Collection;

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

    private static final Long ITEM_ID_1 = 1L;
    private static final Long ITEM_ID_2 = 2L;
    private static final Long ITEM_ID_3 = 3L;
    private static final Long USER_ID_1 = 1L;
    private static final Long USER_ID_2 = 2L;
    private static final Long NON_EXISTENT_USER_ID = 999L;

    private static ItemDto createTestItemDto(Long id, String name, String description, boolean available) {
        return ItemDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .build();
    }

    private static CommentRequestDto createTestCommentRequestDto(String text) {
        return CommentRequestDto.builder()
                .text(text)
                .build();
    }

    @Test
    void createItem_WhenValidData_ShouldCreateItem() {
        ItemDto newItem = createTestItemDto(null, "Фломастеры", "Для раскрас", true);

        ItemDto createdItem = service.addNewItem(newItem, USER_ID_1);

        assertNotNull(createdItem.getId());
        assertEquals(newItem.getName(), createdItem.getName());
        assertEquals(newItem.getDescription(), createdItem.getDescription());
        assertEquals(newItem.getAvailable(), createdItem.getAvailable());
    }

    @Test
    void createItem_WhenUserNotExist_ShouldThrowException() {
        ItemDto newItem = createTestItemDto(null, "Скотч", "Заматывать штуки", true);

        assertThrows(UserNotFoundException.class,
                () -> service.addNewItem(newItem, NON_EXISTENT_USER_ID));
    }

    @Test
    void updateItem_WhenValidOwner_ShouldUpdateItem() {
        ItemDto updateData = createTestItemDto(ITEM_ID_1, "Пассатижи", "Здоровенные", true);
        ItemDto updatedItem = service.updateItem(ITEM_ID_1, updateData, USER_ID_1);

        assertEquals(updateData.getName(), updatedItem.getName());
        assertEquals(updateData.getDescription(), updatedItem.getDescription());
    }

    @Test
    void updateItem_WhenInvalidOwner_ShouldThrowException() {
        ItemDto updateData = createTestItemDto(ITEM_ID_1, "Пила", "Жужужу", true);

        assertThrows(DataConflictException.class,
                () -> service.updateItem(ITEM_ID_1, updateData, USER_ID_2));
    }

    @Test
    void deleteItem_WhenExists_ShouldRemoveItem() {
        service.delete(ITEM_ID_1);

        assertThrows(ItemNotFoundException.class,
                () -> service.findById(ITEM_ID_1, USER_ID_1));
    }

    @Test
    void findById_WhenExists_ShouldReturnItem() {
        ItemDto foundItem = service.findById(ITEM_ID_1, USER_ID_1);

        assertEquals(ITEM_ID_1, foundItem.getId());
        assertNotNull(foundItem.getName());
        assertNotNull(foundItem.getDescription());
    }

    @Test
    void findByOwner_ShouldReturnOwnerItems() {
        Collection<ItemDto> ownerItems = service.findByOwner(USER_ID_2);

        assertEquals(2, ownerItems.size());
        assertTrue(ownerItems.stream().allMatch(item ->
                item.getId().equals(ITEM_ID_2) || item.getId().equals(ITEM_ID_3)));
    }

    @Test
    void createComment_WhenValidBooking_ShouldCreateComment() {
        CommentRequestDto commentDto = createTestCommentRequestDto("Суперские инструменты!");

        CommentDto createdComment = service.createComment(ITEM_ID_1, USER_ID_2, commentDto);

        assertNotNull(createdComment.getId());
        assertEquals(commentDto.getText(), createdComment.getText());
        assertNotNull(createdComment.getCreated());
    }

    @Test
    void createComment_WhenNoBooking_ShouldThrowException() {
        CommentRequestDto commentDto = createTestCommentRequestDto("Отличная вещь!");

        assertThrows(BookingNotFoundException.class,
                () -> service.createComment(ITEM_ID_3, USER_ID_1, commentDto));
    }

    @Test
    void findBySearch_ShouldReturnMatchingItems() {
        Collection<ItemDto> searchResults = service.findBySearch("грифельный");

        assertEquals(1, searchResults.size());
        assertTrue(searchResults.stream().anyMatch(item ->
                item.getName().contains("грифельный") || item.getDescription().contains("грифельный")));
    }

    @Test
    void findBySearch_WhenBlankText_ShouldReturnEmptyList() {
        Collection<ItemDto> searchResults = service.findBySearch("   ");
        assertTrue(searchResults.isEmpty());
    }

    @Test
    void toItemForRequestDto_ShouldConvertCorrectly() {
        User owner = new User(USER_ID_1, "Владелец", "owner@example.com");
        Item item = Item.builder()
                .id(ITEM_ID_1)
                .name("Классный предмет")
                .owner(owner)
                .build();

        RequestedItemDto requested = toRequestedItemDto(item);

        assertEquals(item.getId(), requested.getId());
        assertEquals(item.getName(), requested.getName());
        assertEquals(item.getOwner().getId(), requested.getOwnerId());
    }
}
