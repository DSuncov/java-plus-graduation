package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.configuration.KafkaEventsSimilarityConsumerConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.analyzer.model.Similarity;
import ru.practicum.analyzer.repository.SimilaritiesRepository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventsSimilarityProcessor implements Runnable {

    private final KafkaEventsSimilarityConsumerConfig config;
    private final KafkaConsumer<String, EventSimilarityAvro> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> offsets = new ConcurrentHashMap<>();
    private final SimilaritiesRepository similaritiesRepository;

    private static final Integer OFFSET_INCREMENT = 1;

    @Override
    @KafkaListener(topics = "stats.events-similarity.v1", groupId = "analyzer-events-similarity-group")
    public void run() {
        try (consumer) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(List.of(config.getKafkaProperties().userActionConsumer().topic()));

            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> data = consumer.poll(Duration.ofMillis(config.getKafkaProperties().fetchMaxWaitMs()));
                for (ConsumerRecord<String, EventSimilarityAvro> record : data) {
                    EventSimilarityAvro avro = record.value();

                    if (avro != null) {
                        offsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + OFFSET_INCREMENT));
                        similarityAdded(avro);
                    }
                }

                if (!data.isEmpty()) {
                    consumer.commitSync(offsets);
                }
            }
        } catch (WakeupException ignored) {}
    }

    @Transactional
    private void similarityAdded(EventSimilarityAvro avro) {
        similaritiesRepository.save(Similarity.builder()
                .firstEvent(avro.getEventA())
                .secondEvent(avro.getEventB())
                .similarity(avro.getScore())
                .build());
    }
}
