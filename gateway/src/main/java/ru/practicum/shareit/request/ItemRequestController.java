package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                         @Valid @RequestBody ItemRequestDto itemRequestRequestDto) {
        return itemRequestClient.createRequest(userId, itemRequestRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        return itemRequestClient.getAllForUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getOtherUsersRequests() {
        return itemRequestClient.findAllUsersItemRequest();
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                             @PathVariable Long requestId) {
        return itemRequestClient.getRequest(requestId, userId);
    }
}