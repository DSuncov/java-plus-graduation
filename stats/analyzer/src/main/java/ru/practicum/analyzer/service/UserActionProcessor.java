package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.Interaction;
import ru.practicum.analyzer.repository.InteractionsRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProcessor {

    private final InteractionsRepository interactionsRepository;

    @KafkaListener(
            topics = "stats.user-actions.v1",
            groupId = "analyzer-user-action-group",
            containerFactory = "userActionKafkaListenerContainerFactory")
    @Transactional
    public void processUserAction(UserActionAvro avro) {
        if (avro != null) {
            float rating = getRating(avro);
            double weight = getWeight(avro);

            Optional<Interaction> existing = Optional.ofNullable(interactionsRepository.findByUserIdAndEventId(
                    avro.getUserId(), avro.getEventId()));

            if (existing.isPresent()) {
                Interaction interaction = existing.get();
                if (weight > interaction.getWeight()) {
                    interaction.setRating(rating);
                    interaction.setWeight(weight);
                    interaction.setTimestamp(avro.getTimestamp());
                    interactionsRepository.save(interaction);
                }
            } else {
                Interaction interaction = Interaction.builder()
                        .userId(avro.getUserId())
                        .eventId(avro.getEventId())
                        .rating(rating)
                        .weight(weight)
                        .timestamp(avro.getTimestamp())
                        .build();
                interactionsRepository.save(interaction);
            }
        }
    }

    public float getRating(UserActionAvro avro) {
        return switch (avro.getActionType()) {
            case VIEW -> 0.4f;
            case REGISTER -> 0.8f;
            case LIKE -> 1.0f;
        };
    }

    public double getWeight(UserActionAvro avro) {
        return switch (avro.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
