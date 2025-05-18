package ru.practicum.shareit.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ItemRequest {
    private long id;
    private String description;
    private long requester;
    private LocalDate created;
}
