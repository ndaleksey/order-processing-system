package com.nd.paymentservice.payment.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * @since 2026
 */
public record PaymentSucceededEvent(
        UUID eventId,
        UUID orderId,
        UUID paymentId,
        Instant occurredAt
) {
}
