package com.nd.paymentservice.payment.messaging.outbox;

import com.nd.paymentservice.payment.messaging.kafka.PaymentEventProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@SpringBootTest
@ActiveProfiles("test")
class OutboxPublisherIntegrationTest {

    @MockitoBean
    private PaymentEventProducer paymentEventProducer;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxPublisher outboxPublisher;


    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @Test
    void shouldPersistPublishedAtAfterSuccessfulKafkaSend() {
        var eventId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var createdAt = Instant.now();
        var event = OutboxEvent.createSucceeded(eventId, paymentId, createdAt, "{}");
        outboxEventRepository.save(event);

        @SuppressWarnings("unchecked")
        SendResult<String, String> sendResult = mock(SendResult.class);
        var metadata = mock(RecordMetadata.class);

        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(metadata.topic()).thenReturn("payments");
        when(metadata.partition()).thenReturn(0);
        when(metadata.offset()).thenReturn(10L);

        when(paymentEventProducer.send(event.getAggregatedId(), event.getPayload()))
                        .thenReturn(CompletableFuture.completedFuture(sendResult));

        outboxPublisher.publishPendingEvents();

        var savedEvent = outboxEventRepository.findById(eventId).orElseThrow();

        assertNotNull(savedEvent.getPublishedAt());

        verify(paymentEventProducer).send(paymentId, event.getPayload());
    }

    @Test
    void shouldKeepEventUnpublishedWhenKafkaSendFails() {
        var eventId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var createdAt = Instant.now();
        var event = OutboxEvent.createSucceeded(eventId, paymentId, createdAt, "{}");
        outboxEventRepository.save(event);

        when(paymentEventProducer.send(event.getAggregatedId(), event.getPayload()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka is unavailable")));

        assertThrows(CompletionException.class, () -> outboxPublisher.publishPendingEvents());

        var savedEvent = outboxEventRepository.findById(eventId).orElseThrow();

        assertNull(savedEvent.getPublishedAt());

        verify(paymentEventProducer).send(paymentId, "{}");
    }
}