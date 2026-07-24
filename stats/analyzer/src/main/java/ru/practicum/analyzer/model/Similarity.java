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
@Table(name = "similarities")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Similarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Long id;

    @Column(name = "event1", nullable = false)
    Long firstEvent;

    @Column(name = "event2", nullable = false)
    Long secondEvent;

    @Column(name = "similarity", nullable = false)
    Double similarity;

    @CreationTimestamp
    @Column(name = "ts", nullable = false)
    LocalDateTime timestamp;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Similarity that = (Similarity) object;
        return Objects.equals(firstEvent, that.firstEvent)
                && Objects.equals(secondEvent, that.secondEvent)
                && Objects.equals(similarity, that.similarity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstEvent, secondEvent, similarity);
    }
}
