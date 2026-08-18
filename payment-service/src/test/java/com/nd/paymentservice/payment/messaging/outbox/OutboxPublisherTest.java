package com.nd.paymentservice.payment.messaging.outbox;

import com.nd.paymentservice.payment.messaging.kafka.PaymentEventProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    private OutboxProperties outboxProperties;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    @Test
    void shouldMarkEventAsPublishedWhenKafkaSendSucceeds() {
        // GIVEN
        var aggregateId = UUID.randomUUID();
        var payload = "{}";
        var event = mock(OutboxEvent.class);

        when(event.getAggregatedId()).thenReturn(aggregateId);
        when(event.getPayload()).thenReturn(payload);

        @SuppressWarnings("unchecked")
        ProducerRecord<String, String> producerRecord = mock(ProducerRecord.class);
        var recordMetadata = mock(RecordMetadata.class);


        var result = CompletableFuture.completedFuture(new SendResult<>(producerRecord, recordMetadata));

        when(recordMetadata.topic()).thenReturn("topic");
        when(recordMetadata.partition()).thenReturn(0);
        when(recordMetadata.offset()).thenReturn(1L);

        when(outboxProperties.batchSize()).thenReturn(10);
        when(outboxEventRepository.findByPublishedAtIsNullOrderByCreatedAtAsc(Limit.of(10)))
                .thenReturn(List.of(event));
        when(paymentEventProducer.send(any(UUID.class), anyString())).thenReturn(result);

        // WHEN
        outboxPublisher.publishPendingEvents();

        // THEN
        verify(event).markPublished();
        verify(paymentEventProducer).send(aggregateId, payload);
        verify(outboxEventRepository).findByPublishedAtIsNullOrderByCreatedAtAsc(Limit.of(10));
    }

    @Test
    void shouldNotMarkEventAsPublishedWhenKafkaSendFails() {
        // GIVEN
        var aggregateId = UUID.randomUUID();
        var payload = "{}";
        var event = mock(OutboxEvent.class);

        when(event.getAggregatedId()).thenReturn(aggregateId);
        when(event.getPayload()).thenReturn(payload);

        var result = CompletableFuture.<SendResult<String, String>>failedFuture(new RuntimeException("Kafka unavailable"));

        when(outboxProperties.batchSize()).thenReturn(10);
        when(outboxEventRepository.findByPublishedAtIsNullOrderByCreatedAtAsc(Limit.of(10)))
                .thenReturn(List.of(event));
        when(paymentEventProducer.send(any(UUID.class), anyString()))
                .thenReturn(result);

        // WHEN
        assertThrowsExactly(
                CompletionException.class,
                () -> outboxPublisher.publishPendingEvents(), "Kafka unavailable");

        // THEN
        verify(event, never()).markPublished();
        verify(paymentEventProducer).send(aggregateId, payload);
        verify(outboxEventRepository).findByPublishedAtIsNullOrderByCreatedAtAsc(Limit.of(10));
    }
}