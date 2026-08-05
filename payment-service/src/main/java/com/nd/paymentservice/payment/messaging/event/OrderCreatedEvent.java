package com.nd.paymentservice.payment.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * @since 2026
 */
public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        BigDecimal totalAmount,
        Instant occurredAt
) {
}
