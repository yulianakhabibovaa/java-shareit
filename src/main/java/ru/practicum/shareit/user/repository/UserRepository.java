package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.User;

import java.util.Optional;

public interface UserRepository {
    User create(User user);

    User update(long id,User updUser);

    void delete(long id);

    Optional<User> findById(long id);

    Optional<User> findByEmail(String email);
}
