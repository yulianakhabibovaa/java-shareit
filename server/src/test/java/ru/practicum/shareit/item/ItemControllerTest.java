package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.services.ItemService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private final Long userId = 1L;
    private final Long itemId = 1L;

    private ItemDto.ItemDtoBuilder getTestItemDtoBuilder() {
        return ItemDto.builder()
                .id(itemId)
                .name("TestItemName")
                .description("TestItemDescription")
                .available(true)
                .requestId(1L);
    }

    private CommentDto.CommentDtoBuilder getTestCommentBuilder() {
        return CommentDto.builder()
                .id(1L)
                .text("TestCommentText")
                .authorName("TestAuthorName")
                .created(LocalDateTime.now());
    }

    @Test
    void findById_ShouldReturnItem_WhenValidRequest() throws Exception {
        ItemDto expectedItem = getTestItemDtoBuilder().build();
        when(itemService.findById(anyLong(), anyLong())).thenReturn(expectedItem);

        mvc.perform(get("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedItem.getId()))
                .andExpect(jsonPath("$.name").value(expectedItem.getName()))
                .andExpect(jsonPath("$.description").value(expectedItem.getDescription()));

        verify(itemService).findById(itemId, userId);
    }

    @Test
    void findByOwner_ShouldReturnItemsList_WhenUserExists() throws Exception {
        List<ItemDto> expectedItems = List.of(
                getTestItemDtoBuilder().build(),
                getTestItemDtoBuilder().id(2L).name("AnotherItem").build()
        );

        when(itemService.findByOwner(anyLong())).thenReturn(expectedItems);

        String response = mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(mapper.writeValueAsString(expectedItems), response);
        verify(itemService).findByOwner(userId);
    }

    @Test
    void create_ShouldReturnCreatedItem_WhenValidInput() throws Exception {
        ItemDto inputItem = getTestItemDtoBuilder().build();
        ItemDto expectedItem = getTestItemDtoBuilder().build();

        when(itemService.addNewItem(any(ItemDto.class), anyLong())).thenReturn(expectedItem);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(inputItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedItem.getId()))
                .andExpect(jsonPath("$.name").value(expectedItem.getName()))
                .andExpect(jsonPath("$.available").value(expectedItem.getAvailable()));

        verify(itemService).addNewItem(any(ItemDto.class), eq(userId));
    }

    @Test
    void update_ShouldReturnUpdatedItem_WhenValidInput() throws Exception {
        ItemDto updateDto = getTestItemDtoBuilder().name("UpdatedName").build();
        when(itemService.updateItem(anyLong(), any(ItemDto.class), anyLong())).thenReturn(updateDto);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"));

        verify(itemService).updateItem(eq(itemId), any(ItemDto.class), eq(userId));
    }

    @Test
    void delete_ShouldInvokeService_WhenValidRequest() throws Exception {
        mvc.perform(delete("/items/{itemId}", itemId))
                .andExpect(status().isOk());

        verify(itemService).delete(itemId);
    }

    @Test
    void addComment_ShouldReturnCreatedComment_WhenValidInput() throws Exception {
        CommentDto inputComment = getTestCommentBuilder().id(null).build();
        CommentDto expectedComment = getTestCommentBuilder().build();

        when(itemService.createComment(anyLong(), anyLong(), any(CommentRequestDto.class)))
                .thenReturn(expectedComment);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(inputComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(expectedComment.getText()))
                .andExpect(jsonPath("$.authorName").value(expectedComment.getAuthorName()));

        verify(itemService).createComment(eq(itemId), eq(userId), any(CommentRequestDto.class));
    }

    @Test
    void findBySearch_ShouldReturnFilteredItems_WhenTextProvided() throws Exception {
        List<ItemDto> expectedItems = List.of(
                getTestItemDtoBuilder().build(),
                getTestItemDtoBuilder().id(2L).name("MatchingItem").build()
        );

        when(itemService.findBySearch(anyString())).thenReturn(expectedItems);

        String searchText = "test";
        mvc.perform(get("/items/search")
                        .param("text", searchText)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("TestItemName"));

        verify(itemService).findBySearch(searchText);
    }

    @Test
    void findBySearch_ShouldReturnEmptyList_WhenNoMatches() throws Exception {
        when(itemService.findBySearch(anyString())).thenReturn(Collections.emptyList());

        mvc.perform(get("/items/search")
                        .param("text", "nonexistent")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemService).findBySearch("nonexistent");
    }
}