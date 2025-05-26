package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailInUseException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto newUserDto) {
        if (userRepository.findByEmail(newUserDto.getEmail()).isPresent()) {
            throw new EmailInUseException();
        }
        User user = UserMapper.toUser(newUserDto);
        User createdUser = userRepository.save(user);
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    @Transactional
    public UserDto update(long id, UserDto update) {
        User existingUser = getUser(id);
        if (update.getEmail() != null && !update.getEmail().equals(existingUser.getEmail())) {
            userRepository.findByEmail(update.getEmail())
                    .ifPresent(user -> {
                        if (user.getId() != id) {
                            throw new EmailInUseException();
                        }
                    });
            existingUser.setEmail(update.getEmail());
        }
        if (update.getName() != null && !update.getName().equals(existingUser.getName())) {
            existingUser.setName(update.getName());
        }
        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    @Transactional
    public void delete(long id) {
        getUser(id);
        userRepository.deleteById(id);
    }

    @Override
    public UserDto findById(long id) {
        return UserMapper.toUserDto(getUser(id));
    }

    private User getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
