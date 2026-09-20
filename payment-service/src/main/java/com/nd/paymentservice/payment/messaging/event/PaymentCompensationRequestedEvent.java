package com.nd.paymentservice.payment.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * @since 2026
 */
public record PaymentCompensationRequestedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) {
}
