package com.nd.paymentservice.payment.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {
    private final KafkaTopicsProperties kafkaTopicsProperties;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CompletableFuture<SendResult<String, String>> send(UUID aggregateId, String payload) {
        return kafkaTemplate.send(kafkaTopicsProperties.payments(), aggregateId.toString(), payload);
    }
}
