package ru.practicum.shareit.user.exception;

import ru.practicum.shareit.exception.ValidationException;

public class EmailInUseException extends ValidationException {
    public EmailInUseException() {
        super("Пользователь с таким email уже зарегистрирован");
    }
}
