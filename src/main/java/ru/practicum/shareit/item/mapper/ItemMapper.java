package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@UtilityClass
public class ItemMapper {
    public Item toItem(ItemDto itemDto, User owner) {
        return Item.builder()
                .name(itemDto.getName())
                .owner(owner)
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .request(itemDto.getId() == null ? null : itemDto.getRequest())
                .build();
    }


    public ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest() == null ? null : item.getRequest())
                .build();
    }
}
