package com.nd.orderservice.order.messaging.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @since 2026
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = InventoryReservationSucceededEvent.class,
                name = "RESERVATION_SUCCEEDED"
        ),
        @JsonSubTypes.Type(
                value = InventoryReservationFailedEvent.class,
                name = "RESERVATION_FAILED"
        ),
})
public sealed interface InventoryReservationEvent
        permits InventoryReservationSucceededEvent, InventoryReservationFailedEvent {
    EventType eventType();
}
