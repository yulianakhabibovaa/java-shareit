package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
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

    private static final Long VALID_REQUESTER_ID = 3L;
    private static final Long OTHER_USER_ID = 4L;
    private static final Long NON_EXISTENT_USER_ID = 101L;
    private static final Long NON_EXISTENT_REQUEST_ID = 999L;
    private static final String REQUEST_DESCRIPTION = "спальник для похода";

    @Autowired
    private ItemRequestService service;
    @Autowired
    private UserRepository userRepository;

    private ItemRequestDetailedDto createTestRequest(Long requesterId) {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description(REQUEST_DESCRIPTION)
                .requester(requesterId)
                .build();
        return service.create(requestDto);
    }

    @Test
    void createItemRequest_WhenValidData_ShouldCreateRequest() {
        ItemRequestDetailedDto createdRequest = createTestRequest(VALID_REQUESTER_ID);

        assertNotNull(createdRequest.getId());
        assertEquals(REQUEST_DESCRIPTION, createdRequest.getDescription());
        assertEquals(VALID_REQUESTER_ID, createdRequest.getRequester().getId());

        ItemRequestDetailedDto foundRequest = service.getRequest(createdRequest.getId());
        assertEquals(createdRequest, foundRequest);
    }

    @Test
    void createItemRequest_WhenUserNotExist_ShouldThrowNotFoundException() {
        assertThrows(UserNotFoundException.class,
                () -> createTestRequest(NON_EXISTENT_USER_ID),
                "Should throw NotFoundException for non-existent user");
    }

    @Test
    void findItemRequest_WhenRequestNotExist_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class,
                () -> service.getRequest(NON_EXISTENT_REQUEST_ID),
                "Should throw NotFoundException for non-existent request");
    }

    @Test
    void findAllByUserId_ShouldReturnUserSpecificRequests() {
        createTestRequest(VALID_REQUESTER_ID);
        createTestRequest(VALID_REQUESTER_ID);
        createTestRequest(OTHER_USER_ID);

        Collection<ItemRequestDetailedDto> userRequests = service.getUserRequests(VALID_REQUESTER_ID);

        assertEquals(2, userRequests.size());
        assertTrue(userRequests.stream().allMatch(r -> r.getRequester().getId().equals(VALID_REQUESTER_ID)));
    }

    @Test
    void findAllUsersItemRequest_ShouldReturnOtherUsersRequests() {
        createTestRequest(VALID_REQUESTER_ID);
        createTestRequest(OTHER_USER_ID);
        createTestRequest(OTHER_USER_ID);

        Collection<ItemRequestDetailedDto> otherUsersRequests = service.getOtherUsersRequests(VALID_REQUESTER_ID);

        assertEquals(2, otherUsersRequests.size());
        assertTrue(otherUsersRequests.stream().noneMatch(r -> r.getRequester().getId().equals(VALID_REQUESTER_ID)));
    }

    @Test
    void findAllUsersItemRequest_WhenNoOtherRequests_ShouldReturnEmptyList() {
        createTestRequest(VALID_REQUESTER_ID);
        createTestRequest(VALID_REQUESTER_ID);

        Collection<ItemRequestDetailedDto> otherUsersRequests = service.getOtherUsersRequests(VALID_REQUESTER_ID);

        assertTrue(otherUsersRequests.isEmpty());
    }
}
