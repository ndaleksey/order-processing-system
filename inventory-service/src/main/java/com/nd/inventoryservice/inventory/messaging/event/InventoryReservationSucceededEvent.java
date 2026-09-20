package com.nd.inventoryservice.inventory.messaging.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * @since 2026
 */
public record InventoryReservationSucceededEvent(
        EventType eventType,
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) implements InventoryReservationEvent {
    public InventoryReservationSucceededEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        if (eventType != EventType.RESERVATION_SUCCEEDED) {
            throw new IllegalArgumentException("InventoryReservationSucceededEvent must have RESERVATION_SUCCEEDED type");
        }
    }

    public static InventoryReservationSucceededEvent create(
            UUID eventId,
            UUID orderId,
            Instant occurredAt
    ) {
        return new InventoryReservationSucceededEvent(
                EventType.RESERVATION_SUCCEEDED,
                eventId,
                orderId,
                occurredAt);
    }
}
