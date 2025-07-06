package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;
}
