package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.exception.EmailInUseException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.HashMap;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    final HashMap<Long, User> users;

    @Override
    public User create(User user) {
        user.setId(getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(long id, User update) {
        User existingUser = findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (update.getEmail() != null && !update.getEmail().equals(existingUser.getEmail())) {
            if (users.values().stream()
                    .map(User::getEmail)
                    .anyMatch(it -> it.equals(update.getEmail()))) {
                throw new EmailInUseException();
            }
            existingUser.setEmail(update.getEmail());
        }

        if (update.getName() != null && !update.getName().equals(existingUser.getName())) {
            existingUser.setName(update.getName());
        }

        return users.get(id);
    }

    @Override
    public void delete(long id) {
        users.remove(id);
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(it -> it.getEmail().equals(email))
                .findFirst();
    }

    private long getId() {
        long lastId = users.values().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0L);
        return ++lastId;
    }
}