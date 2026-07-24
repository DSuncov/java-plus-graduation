package ru.practicum.collector.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "collector.kafka.producer")
public record KafkaCollectorProperties(
        String bootstrapServers,
        String keySerializer,
        String valueSerializer,
        String topicStats
) {}
