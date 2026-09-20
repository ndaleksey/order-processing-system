package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.application.command.ReserveInventoryCommand;
import com.nd.inventoryservice.inventory.domain.exception.InventoryReservationException;
import com.nd.inventoryservice.inventory.messaging.idempotency.ProcessedEvent;
import com.nd.inventoryservice.inventory.messaging.idempotency.ProcessedEventRepository;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventFactory;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventRepository;
import com.nd.inventoryservice.inventory.persistence.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @since 2026
 */
@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final OutboxEventFactory eventFactory;
    private final OutboxEventRepository outboxEventRepository;
    private final InventoryItemRepository repository;
    private final ProcessedEventRepository processedEventRepository;

    /**
     * Separate transaction is intentional:
     * reservation rollback must not mark handler transaction as rollback-only.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserve(ReserveInventoryCommand command) {
        command.items().forEach(item ->
                reserve(item.productId(), item.quantity()));

        var outboxEvent = eventFactory.createInventoryReservationSucceeded(command.orderId());

        outboxEventRepository.save(outboxEvent);
        processedEventRepository.save(ProcessedEvent.create(command.eventId()));
    }

    private void reserve(UUID productId, int quantity) {
        var item = repository.findByProductId(productId)
                .orElseThrow(() -> new InventoryReservationException("Inventory item not found: " + productId));

        item.reserve(quantity);
    }
}
