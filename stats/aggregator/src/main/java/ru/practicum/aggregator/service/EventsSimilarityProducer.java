package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.configuration.KafkaEventsSimilarityProducerConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventsSimilarityProducer {

    private final KafkaEventsSimilarityProducerConfig config;
    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    public void send(EventSimilarityAvro eventSimilarity) {

        String topic = config.getKafkaProperties().producer().topic();
        long timestamp = eventSimilarity.getTimestamp().getEpochSecond();
        String key = String.valueOf(eventSimilarity.getEventA()) + eventSimilarity.getEventB();

        kafkaTemplate.send(topic, null, timestamp, key, eventSimilarity)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("Сообщение успешно отправлено");
                    } else {
                        log.error("Сообщение не удалось отправить");
                    }
                });
    }
}
