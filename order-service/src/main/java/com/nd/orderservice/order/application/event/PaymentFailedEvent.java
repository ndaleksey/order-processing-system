package com.nd.orderservice.order.application.event;

import java.time.Instant;
import java.util.UUID;

/**
 * @since 2026
 */
public record PaymentFailedEvent(
        UUID eventId,
        UUID orderId,
        UUID paymentId,
        String failureReason,
        Instant occurredAt
) {
}

