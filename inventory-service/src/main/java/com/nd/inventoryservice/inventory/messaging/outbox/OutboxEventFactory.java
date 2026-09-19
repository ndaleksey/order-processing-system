package com.nd.inventoryservice.inventory.messaging.outbox;

import com.nd.inventoryservice.inventory.messaging.event.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    public OutboxEvent createInventoryReserved(UUID orderId) {
        var eventId = UUID.randomUUID();
        var occurredAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

        var event = InventoryReservedEvent.create(
                eventId,
                orderId,
                occurredAt);

        return OutboxEvent.inventoryReserved(
                event.eventId(),
                event.orderId(),
                occurredAt,
                objectMapper.writeValueAsString(event));
    }
}
