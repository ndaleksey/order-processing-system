package com.nd.inventoryservice.inventory.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * @since 2026
 */
public record InventoryReservedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) {
    public static InventoryReservedEvent create(
            UUID eventId,
            UUID orderId,
            Instant occurredAt
    ) {
        return new InventoryReservedEvent(eventId, orderId, occurredAt);
    }
}
