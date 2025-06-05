package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface  BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
    SELECT (COUNT(b) > 0) FROM Booking b\s
    WHERE b.item.id = :itemId\s
        AND b.status = 'APPROVED'\s
        AND b.start <= :end\s
        AND b.end >= :start\s
    """)
    boolean isAvailable(@Param("itemId")Long itemId, @Param("start")LocalDateTime start, @Param("end")LocalDateTime end);

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    Optional<Booking> findByIdAndItemOwnerId(Long id, Long ownerId);

    List<Booking> findByBookerIdAndEndAfterAndStartBefore(Long bookerId, LocalDateTime now, LocalDateTime now1, Sort sort);

    List<Booking> findByItemOwnerIdAndEndAfterAndStartBefore(Long ownerId, LocalDateTime now, LocalDateTime now1, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStatusEquals(Long bookerId, String status, Sort sort);

    List<Booking> findByItemOwnerIdAndStatusEquals(Long ownerId, String status, Sort sort);

    Optional<Booking> findByBookerAndItem(User user, Item item);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.item.id = :itemId
          AND b.status = 'APPROVED'
          AND b.start < :now
        ORDER BY b.start DESC
        """)
    List<Booking> findLastBooking(@Param("itemId") Long itemId,
                                  @Param("now") LocalDateTime now,
                                  Pageable pageable);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.item.id = :itemId
          AND b.status = 'APPROVED'
          AND b.start > :now
        ORDER BY b.start ASC
        """)
    List<Booking> findNextBooking(@Param("itemId") Long itemId,
                                  @Param("now") LocalDateTime now,
                                  Pageable pageable);
}
