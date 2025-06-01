package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Collection<Item> findByOwnerId(Long ownerId);

    @Query("""
    SELECT i FROM Item i\s
    WHERE\s
        (:text IS NOT NULL AND TRIM(:text) != '')\s
        AND i.available = TRUE\s
        AND (\s
            LOWER(i.name) LIKE LOWER(CONCAT('%', TRIM(:text), '%'))\s
            OR LOWER(i.description) LIKE LOWER(CONCAT('%', TRIM(:text), '%'))\s
        )\s
    """)
    Collection<Item> findByNameOrDescription(@Param("text") String text);
}
