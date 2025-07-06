package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ItemRequestDetailedDto {
    private Long id;
    private String description;
    private UserDto requester;
    private LocalDateTime created;
    private List<RequestedItemDto> items;

    public ItemRequestDetailedDto withItems(List<RequestedItemDto> items) {
        this.items = items;
        return this;
    }
}
