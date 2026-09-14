package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.collector.configuration.KafkaUserActionProducerConfig;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.collector.UserActionProto;
import ru.practicum.collector.mapper.UserActionMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserActionProducer {

    private final KafkaUserActionProducerConfig config;
    private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;
    private final UserActionMapper mapper;

    public void send(UserActionProto action) {
        UserActionAvro userActionAvro = mapper.mapToAvro(action);

        String topic = config.getKafkaCollectorProperties().topicStats();

        //long timestamp = action.getTimestamp().getSeconds() * 1000L;

        Long userId = userActionAvro.getUserId();

        kafkaTemplate.send(topic, userId, userActionAvro)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("Сообщение успешно отправлено. Топик: {}, Partition: {}, Offset: {}, Ключ: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                userId);
                    } else {
                        log.error("Не удалось отправить сообщение в Kafka. Топик: {}, Ключ: {}. Ошибка: {}",
                                topic, userId, exception.getMessage(), exception);
                    }
                });
    }
}
