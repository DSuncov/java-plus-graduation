package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.Similarity;
import ru.practicum.analyzer.repository.SimilaritiesRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventsSimilarityProcessor {

    private final SimilaritiesRepository similaritiesRepository;

    @KafkaListener(
            topics = "stats.events-similarity.v1",
            groupId = "analyzer-events-similarity-group",
        containerFactory = "eventSimilarityKafkaListenerContainerFactory")
    @Transactional
    public void processEventSimilarity(EventSimilarityAvro avro) {
        if (avro != null) {
            Long firstEvent = avro.getEventA();
            Long secondEvent = avro.getEventB();
            Float similarity = (float) avro.getScore();
            Instant timestamp = avro.getTimestamp();

            int updated = similaritiesRepository.updateSimilarity(firstEvent, secondEvent, similarity, timestamp);

            if (updated == 0) {
                similaritiesRepository.save(Similarity.builder()
                        .firstEvent(avro.getEventA())
                        .secondEvent(avro.getEventB())
                        .similarity(avro.getScore())
                        .timestamp(timestamp)
                        .build());
            }
        }
    }
}
