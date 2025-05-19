package ru.practicum.shareit.item.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto addNewItem(ItemDto item, long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));
        return ItemMapper.toItemDto(itemRepository.addNewItem(ItemMapper.toItem(item, ownerId)));
    }

    @Override
    public ItemDto updateItem(long itemId, ItemDto update, long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));
        itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        return ItemMapper.toItemDto(itemRepository.updateItem(itemId, ItemMapper.toItem(update, ownerId)));
    }

    @Override
    public void delete(long itemId) {
        itemRepository.deleteItem(itemId);
    }

    @Override
    public ItemDto findById(long itemId) {
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> findByOwner(long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));
        return itemRepository.findByOwner(ownerId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public Collection<ItemDto> findBySearch(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.findBySearch(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }
}
