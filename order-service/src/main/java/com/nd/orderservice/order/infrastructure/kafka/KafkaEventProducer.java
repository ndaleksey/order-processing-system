package com.nd.orderservice.order.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @since 2026
 */
@RequiredArgsConstructor
@Component
public class KafkaEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CompletableFuture<SendResult<String, String>> send(String topic, UUID aggregateId, String payload) {
        return kafkaTemplate.send(topic, aggregateId.toString(), payload);
    }
}
