package com.nd.inventoryservice.inventory.messaging.consumer;

import com.nd.inventoryservice.inventory.application.InventoryReservationHandler;
import com.nd.inventoryservice.inventory.application.command.ReserveInventoryCommand;
import com.nd.inventoryservice.inventory.application.mapper.ReservationItemMapper;
import com.nd.inventoryservice.inventory.messaging.event.InventoryReservationRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class InventoryReservationRequestedConsumer {

    private final ObjectMapper objectMapper;
    private final InventoryReservationHandler reservationHandler;
    private final ReservationItemMapper itemMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.inventory}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String payload) {
        var event = objectMapper.readValue(payload, InventoryReservationRequestedEvent.class);
        var items = itemMapper.toReservationItems(event.items());
        var command = new ReserveInventoryCommand(event.eventId(), event.orderId(), items);

        reservationHandler.handle(command);
    }
}
