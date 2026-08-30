package ru.practicum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "interactions")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "event_id", nullable = false)
    Long eventId;

    @Column(name = "rating", nullable = false)
    Double rating;

    @CreationTimestamp
    @Column(name = "ts", nullable = false)
    LocalDateTime timestamp;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Interaction that = (Interaction) object;
        return Objects.equals(userId, that.userId)
                && Objects.equals(eventId, that.eventId)
                && Objects.equals(rating, that.rating);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, eventId, rating);
    }
}
