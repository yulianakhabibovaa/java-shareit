package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailInUseException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService service;

    private final Long existingUserId = 2L;

    private UserDto.UserDtoBuilder getUserDtoBuilder() {
        return UserDto.builder()
                .name("Test User")
                .email("test@example.com");
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        UserDto user = service.findById(existingUserId);

        assertNotNull(user);
        assertEquals(existingUserId, user.getId());
        assertNotNull(user.getName());
        assertNotNull(user.getEmail());
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotExists() {
        Long nonExistingUserId = 999L;

        assertThrows(NotFoundException.class, () -> {
            service.findById(nonExistingUserId);
        });
    }

    @Test
    void create_ShouldCreateNewUser_WhenValidInput() {
        UserDto newUser = getUserDtoBuilder().build();

        UserDto createdUser = service.create(newUser);

        assertNotNull(createdUser.getId());
        assertEquals(newUser.getName(), createdUser.getName());
        assertEquals(newUser.getEmail(), createdUser.getEmail());

        UserDto foundUser = service.findById(createdUser.getId());
        assertEquals(createdUser, foundUser);
    }

    @Test
    void update_ShouldUpdateUserName_WhenNewNameProvided() {
        UserDto originalUser = getUserDtoBuilder().build();
        UserDto createdUser = service.create(originalUser);

        createdUser.setName("Updated Name");
        UserDto updatedUser = service.update(createdUser.getId(), createdUser);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(createdUser.getEmail(), updatedUser.getEmail());
        assertEquals(createdUser.getId(), updatedUser.getId());
    }

    @Test
    void update_ShouldUpdateUserEmail_WhenNewEmailProvided() {
        UserDto originalUser = getUserDtoBuilder().build();
        UserDto createdUser = service.create(originalUser);
        String newEmail = "updated@example.com";

        createdUser.setEmail(newEmail);
        UserDto updatedUser = service.update(createdUser.getId(), createdUser);

        assertEquals(newEmail, updatedUser.getEmail());
        assertEquals(createdUser.getName(), updatedUser.getName());
        assertEquals(createdUser.getId(), updatedUser.getId());
    }

    @Test
    void delete_ShouldRemoveUser_WhenUserExists() {
        UserDto newUser = getUserDtoBuilder().build();
        UserDto createdUser = service.create(newUser);

        service.delete(createdUser.getId());

        assertThrows(UserNotFoundException.class, () -> {
            service.findById(createdUser.getId());
        });
    }

    @Test
    void create_ShouldThrowException_WhenEmailAlreadyExists() {
        UserDto firstUser = getUserDtoBuilder().email("duplicate@example.com").build();
        service.create(firstUser);

        UserDto secondUser = getUserDtoBuilder().email("duplicate@example.com").build();

        assertThrows(EmailInUseException.class, () -> {
            service.create(secondUser);
        });
    }
}
