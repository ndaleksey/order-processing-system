package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.application.command.ReserveInventoryCommand;
import com.nd.inventoryservice.inventory.application.exception.InventoryReservationException;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventFactory;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class InventoryReservationHandler {

    private final InventoryReservationService reservationService;
    private final OutboxEventFactory outboxEventFactory;
    private final OutboxEventRepository outboxEventRepository;

    public void handle(ReserveInventoryCommand command) {
        try {
            reservationService.reserve(command);
        } catch (InventoryReservationException e) {
            var event = outboxEventFactory.createInventoryReservationFailed(command.orderId(), e.getMessage());

            outboxEventRepository.save(event);
        }
    }
}
