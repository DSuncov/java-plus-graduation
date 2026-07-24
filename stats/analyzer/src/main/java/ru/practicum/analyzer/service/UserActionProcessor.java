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
import ru.practicum.analyzer.configuration.KafkaUserActionConsumerConfig;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.analyzer.model.Interaction;
import ru.practicum.analyzer.repository.InteractionsRepository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable {

    private final KafkaUserActionConsumerConfig config;
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> offsets = new ConcurrentHashMap<>();
    private final InteractionsRepository interactionsRepository;

    private static final Integer OFFSET_INCREMENT = 1;

    @Override
    @KafkaListener(topics = "stats.user-actions.v1", groupId = "analyzer-user-action-group")
    public void run() {
        try (consumer) {
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            consumer.subscribe(List.of(config.getKafkaProperties().userActionConsumer().topic()));

            while (true) {
                ConsumerRecords<String, UserActionAvro> data = consumer.poll(Duration.ofMillis(config.getKafkaProperties().fetchMaxWaitMs()));
                for (ConsumerRecord<String, UserActionAvro> record : data) {
                    UserActionAvro avro = record.value();

                    if (avro != null) {
                        offsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + OFFSET_INCREMENT));
                        userActionAdded(avro);
                    }
                }

                if (!data.isEmpty()) {
                    consumer.commitSync(offsets);
                }
            }
        } catch (WakeupException ignored) {}
    }

    @Transactional
    private void userActionAdded(UserActionAvro avro) {
        long userId = avro.getUserId();
        long eventId = avro.getEventId();
        double rating = getWeightFromAvro(avro.getActionType());

        Interaction interaction = interactionsRepository.findByUserIdAndEventId(userId, eventId);

        if (rating > interaction.getRating()) {
            interactionsRepository.save(Interaction.builder()
                    .userId(userId)
                    .eventId(eventId)
                    .rating(getWeightFromAvro(avro.getActionType()))
                    .build());
        }
    }

    public double getWeightFromAvro(ActionTypeAvro typeAvro) {
        return switch (typeAvro) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }
}
