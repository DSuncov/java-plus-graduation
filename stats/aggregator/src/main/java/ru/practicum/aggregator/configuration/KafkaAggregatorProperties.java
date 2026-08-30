package ru.practicum.aggregator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aggregator.kafka")
public record KafkaAggregatorProperties(
        String bootstrapServers,

        ConsumerGroup consumer,
        ProducerGroup producer
) {

    public record ConsumerGroup(
            String topic,
            String keyDeserializer,
            String valueDeserializer,
            String groupId,
            String autoOffsetReset,
            Integer fetchMaxWaitMs,
            Integer maxPollRecords,
            Boolean enableAutoCommit
    ) {}

    public record ProducerGroup(
            String topic,
            String keySerializer,
            String valueSerializer
    ) {}
}
