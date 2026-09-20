package com.nd.orderservice.order.infrastructure.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@ExtendWith(MockitoExtension.class)
class KafkaEventProducerTest {
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void shouldSendEventToConfiguredTopicWithAggregateIdAsKey() {
        var orderEventProducer =
                new KafkaEventProducer(kafkaTemplate);
        var expectedFuture = new CompletableFuture<SendResult<String, String>>();
        var aggregateId = UUID.randomUUID();
        var payload = "{}";

        when(kafkaTemplate.send("orders", aggregateId.toString(), payload)).thenReturn(expectedFuture);

        var actualFuture = orderEventProducer.send("orders", aggregateId, payload);

        verify(kafkaTemplate).send("orders", aggregateId.toString(), payload);

        assertSame(expectedFuture, actualFuture);
    }
}
