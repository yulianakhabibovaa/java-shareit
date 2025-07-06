package ru.practicum.shareit.request.exception;

import ru.practicum.shareit.exception.NotFoundException;

public class ItemRequestNotFoundException extends NotFoundException {
    public ItemRequestNotFoundException(Long itemRequestId) {
        super("Запрос предмета с id " + itemRequestId + "не найден");
    }

    public ItemRequestNotFoundException(Long itemRequestId, Long userId) {
        super("Запрос предмета с id " + itemRequestId + "у пользователя" + userId + "не найден");
    }
}