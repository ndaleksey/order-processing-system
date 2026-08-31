package com.nd.paymentservice.payment.messaging.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * @since 2026
 */
public record PaymentFailedEvent(
        EventType eventType,
        UUID eventId,
        UUID orderId,
        UUID paymentId,
        String failureReason,
        Instant occurredAt
) implements PaymentEvent {
    public PaymentFailedEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(failureReason);

        if (eventType != EventType.PAYMENT_FAILED) {
            throw new IllegalArgumentException("PaymentFailedEvent must have PAYMENT_FAILED type");
        }
    }

    public static PaymentFailedEvent create(
            UUID eventId,
            UUID orderId,
            UUID paymentId,
            String failureReason,
            Instant occurredAt
    ) {
        return new PaymentFailedEvent(
                EventType.PAYMENT_FAILED,
                eventId,
                orderId,
                paymentId,
                failureReason,
                occurredAt);
    }
}
