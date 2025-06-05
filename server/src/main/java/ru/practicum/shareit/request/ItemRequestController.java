package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDetailedDto create(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                         @RequestBody ItemRequestDto itemRequestDto) {
        itemRequestDto.setRequester(userId);
        log.info("Создание request {}", itemRequestDto);
        return itemRequestService.create(itemRequestDto);
    }

    @GetMapping
    public Collection<ItemRequestDetailedDto> getUserRequests(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получаем запросы для пользователя с id {}", userId);
        return itemRequestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDetailedDto> getOtherUsersRequests(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        log.info("Получаем список запросов других пользователей для пользователя с id {}", userId);
        return itemRequestService.getOtherUsersRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDetailedDto getRequest(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                             @PathVariable(value = "requestId") Long requestId) {
        log.info("Получаем запрос с id {} для пользователя с id {}", requestId, userId);
        return itemRequestService.getRequest(requestId);
    }

}
