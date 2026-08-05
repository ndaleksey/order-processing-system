package com.nd.paymentservice.payment.payment;

import com.nd.paymentservice.payment.application.PaymentService;
import com.nd.paymentservice.payment.messaging.event.OrderCreatedEvent;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEventRepository;
import com.nd.paymentservice.payment.persistence.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void setUp() {
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
        assertTrue(processedEventRepository.existsById(eventId));
    }

}
