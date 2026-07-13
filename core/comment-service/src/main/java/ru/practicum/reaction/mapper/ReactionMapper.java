package ru.practicum.reaction.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.dto.comment.CommentResponseDto;
import ru.practicum.dto.comment.ReactionResponseDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.reaction.model.Reaction;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ReactionMapper {

    private final CommentMapper commentMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReactionResponseDto toReactionResponseDto(Reaction reaction, UserShortDto evaluator) {
        CommentResponseDto commentResponseDto = commentMapper.toCommentResponseDto(reaction.getComment());

        ReactionResponseDto reactionResponseDto = ReactionResponseDto.builder()
                .id(reaction.getId())
                .voteType(reaction.getVoteType())
                .evaluator(evaluator)
                .commentResponseDto(commentResponseDto)
                .created(formatter.format(reaction.getCreated()))
                .build();

        if (reaction.getUpdated() != null) {
            reactionResponseDto.setUpdated(formatter.format(reaction.getUpdated()));
        }

        return reactionResponseDto;
    }
}
