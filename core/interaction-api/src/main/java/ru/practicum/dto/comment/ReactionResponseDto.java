package ru.practicum.dto.comment;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.user.UserShortDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReactionResponseDto {

    Long id;
    String voteType;
    UserShortDto evaluator;
    CommentResponseDto commentResponseDto;
    String created;
    String updated;
}
