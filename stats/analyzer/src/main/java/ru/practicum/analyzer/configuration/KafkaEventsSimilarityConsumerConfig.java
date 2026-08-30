package ru.practicum.analyzer.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(KafkaAnalyzerProperties.class)
@RequiredArgsConstructor
@Getter
public class KafkaEventsSimilarityConsumerConfig {

    private final KafkaAnalyzerProperties kafkaProperties;

    @Bean("EventsSimilarityConsumerFactory")
    public Properties consumerFactory() {
        KafkaAnalyzerProperties.ConsumerGroup config = kafkaProperties.eventsSimilarityConsumer();
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaProperties.keyDeserializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.valueDeserializer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, config.groupId());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.autoOffsetReset());
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.maxPollRecords());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.enableAutoCommit());
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, kafkaProperties.fetchMaxWaitMs());

        return properties;
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> kafkaConsumerEventSimilarity() {
        Properties properties = consumerFactory();
        return new KafkaConsumer<>(properties);
    }
}
