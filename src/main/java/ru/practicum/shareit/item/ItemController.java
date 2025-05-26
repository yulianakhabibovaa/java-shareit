package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.services.ItemService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService service;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @GetMapping("/{id}")
    public ItemDto get(@PathVariable long id, @RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Получение предмета по id: {}", id);
        return service.findById(id, userId);
    }

    @GetMapping
    public Collection<ItemDto> getByOwnerId(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Получение предметов по id владельца: {}", userId);
        return service.findByOwner(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> getItemBySearch(@RequestParam String text) {
        log.info("Получение предметов по строке поиска: {}", text);
        return service.findBySearch(text);
    }

    @PostMapping
    public ItemDto create(@RequestHeader(X_SHARER_USER_ID) long userId, @RequestBody @Valid ItemDto itemDto) {
        log.info("Создается предмет {} с владельцем {}", itemDto, userId);
        return service.addNewItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(X_SHARER_USER_ID) long userId, @PathVariable long itemId, @RequestBody ItemDto itemDto) {
        log.info("Обновление предмета {} с владельцем {}", itemDto, userId);
        return service.updateItem(itemId, itemDto, userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        log.info("Удаление предмета по id: {}", id);
        service.delete(id);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader(X_SHARER_USER_ID) long userId,
                                    @PathVariable long itemId,
                                    @RequestBody CommentRequestDto commentDto) {
        log.info("Создание комментария к предмету с id: {}", itemId);
        return service.createComment(itemId, userId, commentDto);
    }
}
