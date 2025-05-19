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
    private final HashMap<Long, User> users;
    private final HashMap<String, User> emails;

    @Override
    public User create(User user) {
        user.setId(getId());
        validateEmail(user);
        users.put(user.getId(), user);
        emails.put(user.getEmail(), user);
        return user;
    }

    @Override
    public User update(long id, User update) {
        User existingUser = findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (update.getEmail() != null && !update.getEmail().equals(existingUser.getEmail())) {
            validateEmail(update);
            emails.remove(existingUser.getEmail());
            existingUser.setEmail(update.getEmail());
            emails.put(existingUser.getEmail(), existingUser);
        }

        if (update.getName() != null && !update.getName().equals(existingUser.getName())) {
            existingUser.setName(update.getName());
        }

        return users.get(id);
    }

    @Override
    public void delete(long id) {
        User removed = users.remove(id);
        if (removed != null) {
            emails.remove(removed.getEmail());
        }
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(emails.get(email));
    }

    private long getId() {
        long lastId = users.values().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0L);
        return ++lastId;
    }

    private void validateEmail(User user) {
        if (emails.containsKey(user.getEmail())) {
            throw new EmailInUseException();
        }
    }
}