package ru.practicum.collector.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@EnableConfigurationProperties(KafkaCollectorProperties.class)
@RequiredArgsConstructor
@Configuration
public class KafkaUserActionProducerConfig {

    private final KafkaCollectorProperties kafkaCollectorProperties;

    @Bean
    public ProducerFactory<String, UserActionAvro> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Objects.requireNonNull(kafkaCollectorProperties.bootstrapServers(), "Не указан bootstrap-servers"));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Objects.requireNonNull(kafkaCollectorProperties.keySerializer(), "Не указан сериализатор для ключа."));
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Objects.requireNonNull(kafkaCollectorProperties.valueSerializer(), "Не указан сериализатор для значения."));

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, UserActionAvro> kafkaTemplateUserAction() {
        return new KafkaTemplate<>(producerFactory());
    }
}
