package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final HashMap<Long, Item> items;
    private final Map<Long, List<Long>> ownership;

    @Override
    public Item addNewItem(Item item) {
        item.setId(getId());
        items.put(item.getId(), item);
        ownership.computeIfAbsent(item.getOwnerId(), k -> new ArrayList<>()).add(item.getId());
        return item;
    }

    @Override
    public Item updateItem(long itemId, Item update) {
        Item oldItem = items.get(itemId);
        if (update.getDescription() != null && !update.getDescription().equals(oldItem.getDescription())) {
            oldItem.setDescription(update.getDescription());
        }
        if (update.getName() != null && !update.getName().equals(oldItem.getName())) {
            oldItem.setName(update.getName());
        }
        if (update.getAvailable() != null) {
            oldItem.setAvailable(update.getAvailable());
        }
        return oldItem;
    }

    @Override
    public void deleteItem(long id) {
        Item item = items.remove(id);
        if (item != null) {
            ownership.get(item.getOwnerId()).remove(item.getId());
        }
    }

    @Override
    public Optional<Item> findById(long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Collection<Item> findByOwner(long ownerId) {
        return ownership.getOrDefault(ownerId, Collections.emptyList()).stream().map(items::get).toList();
    }

    @Override
    public Collection<Item> findBySearch(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String textToLower = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(textToLower)
                        || item.getDescription().toLowerCase().contains(textToLower))
                .toList();
    }

    private long getId() {
        long lastId = items.values().stream()
                .mapToLong(Item::getId)
                .max()
                .orElse(0);
        return ++lastId;
    }
}
