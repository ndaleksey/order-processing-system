package com.nd.orderservice.order.application.command;

import java.util.UUID;

/**
 * @since 2026
 */
public record InventoryReservationSucceededCommand(
        UUID eventId,
        UUID orderId
) {

}
