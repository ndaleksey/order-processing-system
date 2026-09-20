package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.application.command.ReserveInventoryCommand;
import com.nd.inventoryservice.inventory.domain.exception.InventoryReservationException;
import com.nd.inventoryservice.inventory.messaging.idempotency.ProcessedEvent;
import com.nd.inventoryservice.inventory.messaging.idempotency.ProcessedEventRepository;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventFactory;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @since 2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryReservationHandler {

    private final InventoryReservationService reservationService;
    private final OutboxEventFactory outboxEventFactory;
    private final OutboxEventRepository outboxEventRepository;
    private final ProcessedEventRepository processedEventRepository;

    public void handle(ReserveInventoryCommand command) {
        if (processedEventRepository.existsById(command.eventId())) {
            log.info("ReserveInventory event already already proceeded: eventId = {}", command.eventId());

            return;
        }

        try {
            reservationService.reserve(command);
        } catch (InventoryReservationException e) {
            var outboxEvent = outboxEventFactory.createInventoryReservationFailed(command.orderId(), e.getMessage());

            outboxEventRepository.save(outboxEvent);
            processedEventRepository.save(ProcessedEvent.create(command.eventId()));
        }
    }
}
