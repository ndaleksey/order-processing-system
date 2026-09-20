package com.nd.paymentservice.payment.application.command;

import java.util.UUID;

/**
 * @since 2026
 */
public record CompensatePaymentCommand(
        UUID eventId,
        UUID orderId
) {
}
