package ru.practicum.shareit.request.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.RequestedItemDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.Collection;
import java.util.List;

@UtilityClass
public class ItemRequestMapper {
    public static List<ItemRequestDetailedDto> toItemRequestDtoList(List<ItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(ItemRequestMapper::toItemRequestDetailedDto)
                .toList();
    }

    public static ItemRequestDetailedDto toItemRequestDetailedDto(ItemRequest itemRequest) {
        if (itemRequest == null) return null;
        return new ItemRequestDetailedDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                UserMapper.toUserDto(itemRequest.getRequester()),
                itemRequest.getCreated(),
                List.of()
        );
    }

    public static RequestedItemDto toRequestedItemDto(Item item) {
        return new RequestedItemDto(item.getId(), item.getName(), item.getOwner().getId());
    }

    public static List<RequestedItemDto> toRequestedItemsDto(Collection<Item> items) {
        return items.stream()
                .map(ItemRequestMapper::toRequestedItemDto)
                .toList();
    }
}
