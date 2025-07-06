package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailInUseException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder().name("Иван Иванов").email("ivan@example.com").build());
    }

    @AfterEach
    void tearDown() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    private UserDto.UserDtoBuilder getUserDtoBuilder() {
        return UserDto.builder()
                .name("Test User")
                .email("test@example.com");
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        UserDto foundUser = service.findById(user.getId());

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        assertNotNull(foundUser.getName());
        assertNotNull(foundUser.getEmail());
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotExists() {
        long nonExistingUserId = 999L;

        assertThrows(NotFoundException.class, () -> service.findById(nonExistingUserId));
    }

    @Test
    void create_ShouldCreateNewUser_WhenValidInput() {
        UserDto newUser = getUserDtoBuilder().build();
        Long createdUserId = service.create(newUser).getId();

        User createdUser = userRepository.findById(createdUserId).get();
        assertNotNull(createdUser.getId());
        assertEquals(createdUserId, createdUser.getId());
        assertEquals(newUser.getName(), createdUser.getName());
        assertEquals(newUser.getEmail(), createdUser.getEmail());
    }

    @Test
    void update_ShouldUpdateUserName_WhenNewNameProvided() {
        UserDto userDto = UserMapper.toUserDto(user);
        userDto.setName("Updated Name");
        service.update(user.getId(), userDto);

        User updatedUser = userRepository.findById(user.getId()).get();
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(user.getEmail(), updatedUser.getEmail());
        assertEquals(user.getId(), updatedUser.getId());
    }

    @Test
    void update_ShouldUpdateUserEmail_WhenNewEmailProvided() {
        String newEmail = "updated@example.com";
        UserDto userDto = UserMapper.toUserDto(user);
        userDto.setEmail(newEmail);
        service.update(user.getId(), userDto);

        User updatedUser = userRepository.findById(user.getId()).get();
        assertEquals(newEmail, updatedUser.getEmail());
        assertEquals(user.getName(), updatedUser.getName());
        assertEquals(user.getId(), updatedUser.getId());
    }

    @Test
    void delete_ShouldRemoveUser_WhenUserExists() {
        service.delete(user.getId());

        assertThrows(UserNotFoundException.class, () -> {
            service.findById(user.getId());
        });
    }

    @Test
    void create_ShouldThrowException_WhenEmailAlreadyExists() {
        UserDto duplicareEmailUser = getUserDtoBuilder().email("ivan@example.com").build();

        assertThrows(EmailInUseException.class, () -> {
            service.create(duplicareEmailUser);
        });
    }
}
