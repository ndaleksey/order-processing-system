package com.nd.inventoryservice.inventory.messaging.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * @since 2026
 */
public record InventoryReservationFailedEvent(
        EventType eventType,
        UUID eventId,
        UUID orderId,
        String failureReason,
        Instant occurredAt
) implements InventoryReservationEvent {
    public InventoryReservationFailedEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        if (eventType != EventType.RESERVATION_FAILED) {
            throw new IllegalArgumentException("InventoryReservationFailedEvent must have RESERVATION_FAILED type");
        }
    }

    public static InventoryReservationFailedEvent create(
            UUID eventId,
            UUID orderId,
            String failureReason,
            Instant occurredAt) {
        return new InventoryReservationFailedEvent(
                EventType.RESERVATION_FAILED,
                eventId,
                orderId,
                failureReason,
                occurredAt);
    }
}
