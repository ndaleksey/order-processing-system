package com.nd.paymentservice.payment.messaging.outbox;

import com.nd.paymentservice.payment.domain.Payment;
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
        return OutboxEvent.createSucceeded(
                UUID.randomUUID(),
                payment.getOrderId(),
                Instant.now(),
                objectMapper.writeValueAsString(payment));
    }

    public OutboxEvent createPaymentFailed(Payment payment) {
        return OutboxEvent.createFailed(
                UUID.randomUUID(),
                payment.getOrderId(),
                Instant.now(),
                objectMapper.writeValueAsString(payment));
    }
}
