package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException e) {
        log.warn("Не найдена сущность {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), "");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(final DataConflictException e) {
        log.warn("Конфликт данных {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), "");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final ValidationException e) {
        log.warn("Некорректный запрос {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), "");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleTypeMismatch(final TypeMismatchException e) {
        log.warn("Передан некорректный параметр {}", e.getMessage());
        return new ErrorResponse(e.getMessage(), "");
    }
}
