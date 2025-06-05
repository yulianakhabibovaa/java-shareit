package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
import ru.practicum.shareit.item.dto.ItemDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemClient client;
    static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItem(@RequestHeader(X_SHARER_USER_ID) long userId,
                                          @PathVariable long id) {
        return client.getItem(id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemByOwner(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        return client.getItemByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestParam String text) {
        return client.searchItem(text);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(X_SHARER_USER_ID) long userId,
                                             @RequestBody @Valid ItemDto itemDto) {
        return client.createItem(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestHeader(X_SHARER_USER_ID) long userId,
                                         @PathVariable long id,
                                         @RequestBody ItemDto itemDto) {
        itemDto.setId(id);
        return client.updateItem(itemDto, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable long id) {
        client.deleteItem(id);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                @PathVariable long id,
                                                @RequestBody CommentDto comment) {
        return client.addComment(userId, id, comment);

    }
}
