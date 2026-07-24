package ru.practicum.aggregator.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;

@Getter
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaAggregatorProperties.class)
public class KafkaUserActionConsumerConfig {

    private final KafkaAggregatorProperties kafkaProperties;

    @Bean("UserActionConsumerFactory")
    public ConsumerFactory<Long, UserActionAvro> actionConsumerFactory() {
        KafkaAggregatorProperties.ConsumerGroup config = kafkaProperties.consumer();
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.keyDeserializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.valueDeserializer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, config.groupId());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.autoOffsetReset());
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.maxPollRecords());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.enableAutoCommit());
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, config.fetchMaxWaitMs());

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, UserActionAvro> userActionKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, UserActionAvro> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(actionConsumerFactory());
        return factory;
    }
}
