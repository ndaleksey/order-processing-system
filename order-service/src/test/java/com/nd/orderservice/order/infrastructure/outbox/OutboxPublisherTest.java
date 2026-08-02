package com.nd.orderservice.order.infrastructure.outbox;

import com.nd.orderservice.order.infrastructure.kafka.OrderEventProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {
    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    @Test
    void shouldMarkEventAsPublishedWhenKafkaSendSucceeds() {
        var orderId = UUID.randomUUID();
        var payload = "{}";
        var event = OutboxEvent.orderCreated(orderId, payload);

        @SuppressWarnings("unchecked")
        SendResult<String, String> sendResult = Mockito.mock(SendResult.class);

        var metadata = Mockito.mock(RecordMetadata.class);

        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(metadata.topic()).thenReturn("order.events");
        when(metadata.partition()).thenReturn(0);
        when(metadata.offset()).thenReturn(10L);

        var completableFuture = CompletableFuture.completedFuture(sendResult);

        when(outboxEventRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of(event));

        when(orderEventProducer.send(orderId, payload))
                .thenReturn(completableFuture);

        outboxPublisher.publishPendingEvents();

        verify(orderEventProducer).send(orderId, payload);

        assertNotNull(event.getPublishedAt());
    }

    @Test
    void shouldNotMarkEventAsPublishedWhenKafkaSendFails() {
        var orderId = UUID.randomUUID();
        var payload = "{}";
        var event = OutboxEvent.orderCreated(orderId, payload);

        var completableFuture = CompletableFuture.<SendResult<String, String>>failedFuture(
                new RuntimeException("Kafka unavailable"));

        when(outboxEventRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of(event));

        when(orderEventProducer.send(orderId, payload))
                .thenReturn(completableFuture);

        assertThrows(CompletionException.class,
                () -> outboxPublisher.publishPendingEvents());

        verify(orderEventProducer).send(orderId, payload);

        assertNull(event.getPublishedAt());
    }
}
