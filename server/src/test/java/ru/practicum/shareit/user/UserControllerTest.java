package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final Long userId = 1L;
    private final String userName = "Test User";
    private final String userEmail = "test@example.com";

    private UserDto.UserDtoBuilder getUserDtoBuilder() {
        return UserDto.builder()
                .id(userId)
                .name(userName)
                .email(userEmail);
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        UserDto expectedUser = getUserDtoBuilder().build();
        when(userService.findById(userId)).thenReturn(expectedUser);

        mvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.intValue())))
                .andExpect(jsonPath("$.name", is(userName)))
                .andExpect(jsonPath("$.email", is(userEmail)));

        verify(userService).findById(userId);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenValidInput() throws Exception {
        UserDto updateDto = getUserDtoBuilder().name("Updated Name").build();
        UserDto expectedUser = getUserDtoBuilder().name("Updated Name").build();

        when(userService.update(anyLong(), any(UserDto.class))).thenReturn(expectedUser);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")));
        verify(userService, times(1)).update(anyLong(), any());
    }

    @Test
    void createUser_ShouldReturnCreatedUser_WhenValidInput() throws Exception {
        UserDto inputDto = getUserDtoBuilder().id(null).build();
        UserDto expectedUser = getUserDtoBuilder().build();

        when(userService.create(any(UserDto.class))).thenReturn(expectedUser);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.email", is(userEmail)));

        verify(userService).create(argThat(dto ->
                dto.getId() == null &&
                        dto.getEmail().equals(userEmail)));
    }

    @Test
    void deleteUser_ShouldInvokeService_WhenUserExists() throws Exception {
        mvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService).delete(userId);
    }
}
