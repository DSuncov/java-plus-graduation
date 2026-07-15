package ru.practicum.comment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JoinColumn(name = "commentator_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comments_users"))
    Long commentatorId;

    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comments_events"))
    Long eventId;

    @Column(name = "created")
    LocalDateTime created;

    @Column(name = "text")
    String text;
}