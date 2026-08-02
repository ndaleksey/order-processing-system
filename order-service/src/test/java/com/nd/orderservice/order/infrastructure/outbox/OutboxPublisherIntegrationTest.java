package com.nd.orderservice.order.infrastructure.outbox;

import com.nd.orderservice.order.infrastructure.kafka.OrderEventProducer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
class OutboxPublisherIntegrationTest {

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private tools.jackson.databind.ObjectMapper objectMapper;

    @MockitoBean
    private OrderEventProducer orderEventProducer;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldPersistPublishedAtAfterSuccessfulKafkaSend() {
        outboxEventRepository.deleteAll();

        var orderId = UUID.fromString(
                "97630c6f-a2a1-4adc-ab0c-02f5d45b3699"
        );

        var payload = """
                {
                  "orderId": "97630c6f-a2a1-4adc-ab0c-02f5d45b3699",
                  "customerId": "c4bc48d5-9fdf-48a8-af0c-fb63035f7093"
                }
                """;

        var event = OutboxEvent.orderCreated(orderId, payload);
        var savedEvent = outboxEventRepository.saveAndFlush(event);

        @SuppressWarnings("unchecked")
        SendResult<String, String> sendResult = mock(SendResult.class);

        var metadata = mock(RecordMetadata.class);

        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(metadata.topic()).thenReturn("order.events");
        when(metadata.partition()).thenReturn(0);
        when(metadata.offset()).thenReturn(10L);

        when(orderEventProducer.send(eq(orderId), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        outboxPublisher.publishPendingEvents();

        entityManager.clear();

        var persistedEvent = outboxEventRepository
                .findById(savedEvent.getId())
                .orElseThrow();

        assertNotNull(persistedEvent.getPublishedAt());
        assertEquals(orderId, persistedEvent.getAggregateId());

        assertEquals(
                objectMapper.readTree(payload),
                objectMapper.readTree(persistedEvent.getPayload())
        );

        verify(orderEventProducer)
                .send(eq(orderId), anyString());

        var pendingEvents = outboxEventRepository
                .findTop10ByPublishedAtIsNullOrderByCreatedAtAsc();

        assertTrue(pendingEvents.isEmpty());
    }
}