package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@UtilityClass
public class ItemMapper {
    public Item toItem(ItemDto itemDto, long ownerId) {
        return new Item(itemDto.getId(),
                ownerId,
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                null);
    }


    public ItemDto toItemDto(Item item) {
        return new ItemDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null);
    }
}
