package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
class ItemRequestServiceTest {

    private static final Long NON_EXISTENT_USER_ID = 101L;
    private static final Long NON_EXISTENT_REQUEST_ID = 999L;
    private static final String REQUEST_DESCRIPTION = "спальник для похода";

    @Autowired
    private ItemRequestService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private EntityManager entityManager;

    private User requester;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requester = userRepository.save(
                User.builder().id(3L).name("Test Requester").email("requester@example.com").build()
        );
        otherUser = userRepository.save(
                User.builder().id(4L).name("Other User").email("other@example.com").build()
        );
    }

    @AfterEach
    void tearDown() {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE requests RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE items RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY").executeUpdate();
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    private ItemRequestDetailedDto createTestRequest(Long requesterId) {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description(REQUEST_DESCRIPTION)
                .requester(requesterId)
                .build();
        return service.create(requestDto);
    }

    @Test
    void createItemRequest_WhenValidData_ShouldCreateRequest() {
        ItemRequestDetailedDto createdRequest = createTestRequest(requester.getId());
        ItemRequest savedRequest = itemRequestRepository.findById(createdRequest.getId()).get();

        assertNotNull(savedRequest.getId());
        assertEquals(REQUEST_DESCRIPTION, savedRequest.getDescription());
        assertEquals(requester.getId(), savedRequest.getRequester().getId());
    }

    @Test
    void createItemRequest_WhenUserNotExist_ShouldThrowNotFoundException() {
        assertThrows(UserNotFoundException.class,
                () -> createTestRequest(NON_EXISTENT_USER_ID));
    }

    @Test
    void findItemRequest_WhenRequestNotExist_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class,
                () -> service.getRequest(NON_EXISTENT_REQUEST_ID));
    }

    @Test
    void findAllByUserId_ShouldReturnUserSpecificRequests() {
        createTestRequest(requester.getId());
        createTestRequest(requester.getId());
        createTestRequest(otherUser.getId());

        Collection<ItemRequestDetailedDto> userRequests = service.getUserRequests(requester.getId());

        assertEquals(2, userRequests.size());
        assertTrue(userRequests.stream().allMatch(r -> r.getRequester().getId().equals(requester.getId())));
    }

    @Test
    void findAllUsersItemRequest_ShouldReturnOtherUsersRequests() {
        createTestRequest(requester.getId());
        createTestRequest(otherUser.getId());
        createTestRequest(otherUser.getId());

        Collection<ItemRequestDetailedDto> otherUsersRequests = service.getOtherUsersRequests(requester.getId());

        assertEquals(2, otherUsersRequests.size());
        assertTrue(otherUsersRequests.stream().noneMatch(r -> r.getRequester().getId().equals(requester.getId())));
    }

    @Test
    void findAllUsersItemRequest_WhenNoOtherRequests_ShouldReturnEmptyList() {
        createTestRequest(requester.getId());
        createTestRequest(requester.getId());

        Collection<ItemRequestDetailedDto> otherUsersRequests = service.getOtherUsersRequests(requester.getId());

        assertTrue(otherUsersRequests.isEmpty());
    }
}