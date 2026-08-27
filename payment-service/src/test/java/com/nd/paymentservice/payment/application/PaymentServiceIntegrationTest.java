package com.nd.paymentservice.payment.application;

import com.nd.paymentservice.payment.domain.PaymentStatus;
import com.nd.paymentservice.payment.messaging.event.OrderCreatedEvent;
import com.nd.paymentservice.payment.messaging.event.PaymentFailedEvent;
import com.nd.paymentservice.payment.messaging.event.PaymentSucceededEvent;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEventRepository;
import com.nd.paymentservice.payment.messaging.outbox.OutboxEventRepository;
import com.nd.paymentservice.payment.persistence.PaymentRepository;
import com.nd.paymentservice.payment.provider.FakePaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 2026
 */
@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        processedEventRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    void shouldProcessSameEventOnlyOnce() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();

        var event = new OrderCreatedEvent(eventId, orderId, UUID.randomUUID(), BigDecimal.ONE, Instant.now());

        paymentService.handleOrderCreated(event);
        paymentService.handleOrderCreated(event);

        assertEquals(1, paymentRepository.count());
        assertEquals(1, processedEventRepository.count());
        assertEquals(1, outboxEventRepository.count());

        var payment = paymentRepository.findAll().getFirst();
        var outboxEvent = outboxEventRepository.findAll().getFirst();

        var payloadEvent = objectMapper.readValue(outboxEvent.getPayload(), PaymentSucceededEvent.class);
        assertEquals(payment.getOrderId(), payloadEvent.orderId());
        assertEquals(payment.getId(), payloadEvent.paymentId());
        assertEquals(outboxEvent.getId(), payloadEvent.eventId());
        assertNotNull(payloadEvent.occurredAt());
        assertEquals(outboxEvent.getCreatedAt(), payloadEvent.occurredAt());

        assertEquals(payment.getId(), outboxEvent.getAggregatedId());
        assertEquals("PAYMENT_SUCCEEDED", outboxEvent.getType());
        assertNull(outboxEvent.getPublishedAt());

        assertTrue(processedEventRepository.existsById(eventId));
    }

    @Test
    void shouldCreateFailedPaymentAndOutboxEventWhenProviderFails() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();

        // GIVEN
        var event = new OrderCreatedEvent(eventId, orderId, UUID.randomUUID(), BigDecimal.valueOf(20_000), Instant.now());

        // WHEN
        paymentService.handleOrderCreated(event);

        // THEN
        var payments = paymentRepository.findAll();

        assertEquals(1, payments.size());

        var payment = payments.getFirst();

        assertEquals(PaymentStatus.FAILED, payment.getStatus());

        var processedEvents = processedEventRepository.findAll();

        assertEquals(1, processedEvents.size());

        var outboxEvents = outboxEventRepository.findAll();

        assertEquals(1, outboxEvents.size());

        var outboxEvent = outboxEvents.getFirst();

        assertEquals("PAYMENT_FAILED", outboxEvent.getType());
        assertEquals(payment.getId(), outboxEvent.getAggregatedId());
        assertNull(outboxEvent.getPublishedAt());

        var payloadEvent = objectMapper.readValue(outboxEvent.getPayload(), PaymentFailedEvent.class);

        assertEquals(payment.getOrderId(), payloadEvent.orderId());
        assertEquals(payment.getId(), payloadEvent.paymentId());
        assertEquals(outboxEvent.getId(), payloadEvent.eventId());
        assertEquals(FakePaymentProvider.INSUFFICIENT_FUNDS, payloadEvent.failureReason());
        assertEquals(outboxEvent.getCreatedAt(), payloadEvent.occurredAt());
    }


}
