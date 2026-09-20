package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.application.command.ReserveInventoryCommand;
import com.nd.inventoryservice.inventory.application.exception.InventoryReservationException;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventFactory;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventRepository;
import com.nd.inventoryservice.inventory.persistence.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @since 2026
 */
@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final OutboxEventFactory eventFactory;
    private final OutboxEventRepository eventRepository;
    private final InventoryItemRepository repository;

    @Transactional
    public void reserve(ReserveInventoryCommand command) {
        command.items().forEach(item ->
                reserve(item.productId(), item.quantity()));

        var event = eventFactory.createInventoryReservationSucceeded(command.orderId());

        eventRepository.save(event);
    }

    private void reserve(UUID productId, int quantity) {
        var item = repository.findByProductId(productId)
                .orElseThrow(() -> new InventoryReservationException("Inventory item not found: " + productId));

        item.reserve(quantity);
    }
}
