package ru.practicum.aggregator.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.HashMap;
import java.util.Map;

@Getter
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaAggregatorProperties.class)
public class KafkaEventsSimilarityProducerConfig {

    private final KafkaAggregatorProperties kafkaProperties;

    public ProducerFactory<String, EventSimilarityAvro> producerFactory() {
        KafkaAggregatorProperties.ProducerGroup config = kafkaProperties.producer();
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.keySerializer());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.valueSerializer());

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, EventSimilarityAvro> kafkaTemplateSimilarity() {
        return new KafkaTemplate<>(producerFactory());
    }
}
