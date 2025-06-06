package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDetailedDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
class ItemRequestControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ItemRequestService requestService;

    private final Long userId = 1L;
    private final Long requestId = 1L;
    private final LocalDateTime now = LocalDateTime.now();

    private ItemRequestDetailedDto.ItemRequestDetailedDtoBuilder getItemRequestDetailedDtoBuilder() {
        return ItemRequestDetailedDto.builder()
                .id(requestId)
                .description("TestItemRequestDescription")
                .requester(null)
                .created(now)
                .items(List.of());
    }

    private ItemRequestDto.ItemRequestDtoBuilder getItemRequestDtoBuilder() {
        return ItemRequestDto.builder()
                .description("TestItemRequestDescription")
                .created(now);
    }

    @Test
    void create_ShouldReturnCreatedRequest_WhenValidInput() throws Exception {
        ItemRequestDto inputDto = getItemRequestDtoBuilder().build();
        ItemRequestDetailedDto expectedDto = getItemRequestDetailedDtoBuilder().build();

        when(requestService.create(any(ItemRequestDto.class))).thenReturn(expectedDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value(inputDto.getDescription()));

        verify(requestService).create(any(ItemRequestDto.class));
    }

    @Test
    void findAllByUserId_ShouldReturnRequestsList_WhenUserExists() throws Exception {
        ItemRequestDetailedDto requestDto = getItemRequestDetailedDtoBuilder().build();
        List<ItemRequestDetailedDto> expectedList = List.of(requestDto);

        when(requestService.getUserRequests(userId)).thenReturn(expectedList);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(requestId));

        verify(requestService).getUserRequests(userId);
    }

    @Test
    void findItemRequestById_ShouldReturnRequest_WhenValidIds() throws Exception {
        ItemRequestDetailedDto expectedDto = getItemRequestDetailedDtoBuilder().build();

        when(requestService.getRequest(requestId)).thenReturn(expectedDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").exists());

        verify(requestService).getRequest(requestId);
    }

    @Test
    void findAllUsersItemRequest_ShouldReturnOthersRequests_WhenValidUserId() throws Exception {
        ItemRequestDetailedDto requestDto = getItemRequestDetailedDtoBuilder().build();
        List<ItemRequestDetailedDto> expectedList = List.of(requestDto);

        when(requestService.getOtherUsersRequests(userId)).thenReturn(expectedList);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(requestId));

        verify(requestService).getOtherUsersRequests(userId);
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoRequestsExist() throws Exception {
        when(requestService.getUserRequests(userId)).thenReturn(List.of());

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(requestService).getUserRequests(userId);
    }
}