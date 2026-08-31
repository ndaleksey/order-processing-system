package com.nd.paymentservice.payment.messaging.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * @since 2026
 */
public record PaymentSucceededEvent(
        EventType eventType,
        UUID eventId,
        UUID orderId,
        UUID paymentId,
        Instant occurredAt
) implements PaymentEvent {
    public PaymentSucceededEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        if (eventType != EventType.PAYMENT_SUCCEEDED) {
            throw new IllegalArgumentException("PaymentSucceededEvent must have PAYMENT_SUCCEEDED type");
        }
    }

    public static PaymentSucceededEvent create(
            UUID eventId,
            UUID orderId,
            UUID paymentId,
            Instant occurredAt) {
        return new PaymentSucceededEvent(EventType.PAYMENT_SUCCEEDED, eventId, orderId, paymentId, occurredAt);
    }
}
