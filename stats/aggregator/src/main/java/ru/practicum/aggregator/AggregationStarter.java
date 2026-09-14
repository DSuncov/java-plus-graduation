package ru.practicum.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.aggregator.service.EventsSimilarityHandler;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaTemplate<Long, EventSimilarityAvro> kafkaTemplate;
    private final EventsSimilarityHandler handler;

    @KafkaListener(
            topics = "stats.user-actions.v1",
            groupId = "aggregator-group",
            containerFactory = "userActionKafkaListenerContainerFactory")
    @Transactional
    public void processUserAction(UserActionAvro userActionAvro) {
        if (userActionAvro == null) {
            log.warn("Получено пустое сообщение, пропускаем");
            return;
        }

        log.debug("Обработка действия пользователя: {}", userActionAvro);

        List<EventSimilarityAvro> similarities = handler.calculateSimilarity(userActionAvro);

        if (similarities != null && !similarities.isEmpty()) {
            for (EventSimilarityAvro similarity : similarities) {
                kafkaTemplate.send("stats.events-similarity.v1", similarity);
            }
            log.info("Успешно отправлено {} сообщений о схожести", similarities.size());
        }
    }
}
