package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exception.EmailInUseException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto newUserDto) {
        if (userRepository.findByEmail(newUserDto.getEmail()).isPresent()) {
            throw new EmailInUseException();
        }
        User user = UserMapper.toUser(newUserDto);
        User createdUser = userRepository.create(user);
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto update(long id, UserDto userUpdDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (userUpdDto.getEmail() != null && !userUpdDto.getEmail().equals(existingUser.getEmail())) {
            userRepository.findByEmail(userUpdDto.getEmail())
                    .ifPresent(user -> {
                        if (user.getId() != id) {
                            throw new EmailInUseException();
                        }
                    });
        }

        User userToUpdate = UserMapper.toUser(userUpdDto);
        User updatedUser = userRepository.update(id, userToUpdate);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException(id);
        }
        userRepository.delete(id);
    }

    @Override
    public UserDto findById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserMapper.toUserDto(user);
    }
}
