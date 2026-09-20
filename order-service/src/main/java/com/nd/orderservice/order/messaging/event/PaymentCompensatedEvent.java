package com.nd.orderservice.order.messaging.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * @since 2026
 */
public record PaymentCompensatedEvent(
        EventType eventType,
        UUID eventId,
        UUID orderId,
        UUID paymentId,
        Instant occurredAt
) implements PaymentEvent {
    public PaymentCompensatedEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(paymentId, "paymentId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        if (eventType != EventType.PAYMENT_COMPENSATED) {
            throw new IllegalArgumentException("PaymentCompensatedEvent must have PAYMENT_COMPENSATED type");
        }
    }

    public static PaymentCompensatedEvent create(
            UUID eventId,
            UUID orderId,
            UUID paymentId,
            Instant occurredAt) {
        return new PaymentCompensatedEvent(EventType.PAYMENT_COMPENSATED, eventId, orderId, paymentId, occurredAt);
    }
}
