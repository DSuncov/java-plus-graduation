package ru.practicum.aggregator;

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
import ru.practicum.aggregator.configuration.KafkaUserActionConsumerConfig;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.aggregator.service.EventsSimilarityHandler;
import ru.practicum.aggregator.service.EventsSimilarityProducer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaUserActionConsumerConfig config;
    private final EventsSimilarityHandler handler;
    private final EventsSimilarityProducer producer;
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> offsets = new ConcurrentHashMap<>();

    @KafkaListener(topics = "stats.user-actions.v1", groupId = "aggregator-group")
    public void start() {

        try (consumer) {

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            consumer.subscribe(List.of(config.getKafkaProperties().consumer().topic()));

            while (true) {
                ConsumerRecords<String, UserActionAvro> data = consumer.poll(Duration.ofMillis(config.getKafkaProperties().consumer().fetchMaxWaitMs()));

                for (ConsumerRecord<String, UserActionAvro> record : data) {
                    UserActionAvro userActionAvro = record.value();

                    List<EventSimilarityAvro> similarities = handler.calculateSimilarity(userActionAvro);

                    for (EventSimilarityAvro similarity : similarities) {
                        producer.send(similarity);
                        offsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));
                    }
                }
            }

        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки действий от пользователей", e);
        } finally {
            try {
                consumer.commitSync(offsets);
            } catch (Exception e) {
                log.error("Смещения не зафиксированы");
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }
}
