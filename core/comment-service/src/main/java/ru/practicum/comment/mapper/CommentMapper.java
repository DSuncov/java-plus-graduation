package ru.practicum.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.comment.model.Comment;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentRequestDto;
import ru.practicum.dto.comment.CommentResponseDto;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "commentatorId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    Comment toComment(CommentRequestDto commentRequestDto);

    @Mapping(source = "commentatorId", target = "commentatorId")
    CommentResponseDto toCommentResponseDto(Comment comment);

    @Mapping(source = "commentatorId", target = "commentatorId")
    @Mapping(source = "eventId", target = "eventId")
    CommentDto toCommentDto(Comment comment);
}