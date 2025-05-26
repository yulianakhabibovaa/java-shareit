package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

@UtilityClass
public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getId(),
                comment.getItem().getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated());
    }
}
