package com.nd.paymentservice.payment.messaging.outbox;

import com.nd.paymentservice.payment.domain.Payment;
import com.nd.paymentservice.payment.messaging.event.PaymentFailedEvent;
import com.nd.paymentservice.payment.messaging.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class OutboxEventFactory {
    private final ObjectMapper objectMapper;

    public OutboxEvent createPaymentSucceeded(Payment payment) {
        var eventId = UUID.randomUUID();
        var occurredAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

        var event = new PaymentSucceededEvent(
                eventId,
                payment.getOrderId(),
                payment.getId(),
                occurredAt);

        return OutboxEvent.createSucceeded(
                event.eventId(),
                payment.getId(),
                occurredAt,
                objectMapper.writeValueAsString(event));
    }

    public OutboxEvent createPaymentFailed(Payment payment, String reason) {
        var eventId = UUID.randomUUID();
        var occurredAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

        var event = new PaymentFailedEvent(
                eventId,
                payment.getOrderId(),
                payment.getId(),
                reason,
                occurredAt);

        return OutboxEvent.createFailed(
                event.eventId(),
                payment.getId(),
                occurredAt,
                objectMapper.writeValueAsString(event));
    }
}
