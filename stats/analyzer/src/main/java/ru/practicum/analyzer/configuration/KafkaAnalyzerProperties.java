package ru.practicum.analyzer.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaAnalyzerProperties(
        String bootstrapServers,
        String keyDeserializer,
        Boolean enableAutoCommit,
        Integer fetchMaxWaitMs,

        ConsumerGroup userActionConsumer,
        ConsumerGroup eventsSimilarityConsumer
) {
    public record ConsumerGroup(
            String topic,
            String groupId,
            String valueDeserializer,
            String autoOffsetReset,
            Integer maxPollRecords
    ) {}
}
