package ru.practicum.shareit.user.exception;

import ru.practicum.shareit.exception.DataConflictException;

public class EmailInUseException extends DataConflictException {
    public EmailInUseException() {
        super("Пользователь с таким email уже зарегистрирован");
    }
}
