package com.nd.paymentservice.payment.messaging.outbox;

import com.nd.paymentservice.payment.domain.Payment;
import com.nd.paymentservice.payment.messaging.event.PaymentFailedEvent;
import com.nd.paymentservice.payment.messaging.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class OutboxEventFactory {
    private final ObjectMapper objectMapper;

    public OutboxEvent createPaymentSucceeded(Payment payment) {
        var event = new PaymentSucceededEvent(
                UUID.randomUUID(),
                payment.getOrderId(),
                payment.getId(),
                Instant.now());

        return OutboxEvent.createSucceeded(
                UUID.randomUUID(),
                payment.getId(),
                Instant.now(),
                objectMapper.writeValueAsString(event));
    }

    public OutboxEvent createPaymentFailed(Payment payment, String reason) {
        var event = new PaymentFailedEvent(
                UUID.randomUUID(),
                payment.getOrderId(),
                payment.getId(),
                reason,
                Instant.now());

        return OutboxEvent.createFailed(
                UUID.randomUUID(),
                payment.getId(),
                Instant.now(),
                objectMapper.writeValueAsString(event));
    }
}
