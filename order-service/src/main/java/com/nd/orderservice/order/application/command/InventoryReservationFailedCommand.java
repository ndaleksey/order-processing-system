package com.nd.orderservice.order.application.command;

import java.util.UUID;

/**
 * @since 2026
 */
public record InventoryReservationFailedCommand(
        UUID eventId,
        UUID orderId,
        String failureReason
) {

}
