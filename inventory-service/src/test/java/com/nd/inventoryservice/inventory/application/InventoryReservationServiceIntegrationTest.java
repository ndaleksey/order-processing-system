package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.application.command.ReserveInventoryCommand;
import com.nd.inventoryservice.inventory.application.model.ReservationItem;
import com.nd.inventoryservice.inventory.domain.InventoryItem;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventRepository;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventType;
import com.nd.inventoryservice.inventory.persistence.InventoryItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 2026
 */
@SpringBootTest
@ActiveProfiles("test")
class InventoryReservationServiceIntegrationTest {
    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private InventoryItemRepository repository;

    @Autowired
    private InventoryReservationService service;

    @Test
    void shouldReserveInventoryAndCreateOutboxEvent() {

        //GIVEN
        var productAId = UUID.randomUUID();
        var productBId = UUID.randomUUID();

        var productAItem = InventoryItem.create(productAId, 10);
        var productBItem = InventoryItem.create(productBId, 20);

        repository.save(productAItem);
        repository.save(productBItem);

        var orderId = UUID.randomUUID();
        var reservationAItem = new ReservationItem(productAId, 3);
        var reservationBItem = new ReservationItem(productBId, 5);

        var command = new ReserveInventoryCommand(orderId, List.of(reservationAItem, reservationBItem));

        // WHEN
        service.reserve(command);

        // THEN
        var savedAItem = repository.findByProductId(productAId).orElseThrow();

        assertEquals(7, savedAItem.getAvailableQuantity());

        var savedBItem = repository.findByProductId(productBId).orElseThrow();

        assertEquals(15, savedBItem.getAvailableQuantity());

        var outboxEvent = outboxEventRepository
                .findByAggregateIdAndTypeAndPublishedAtIsNull(
                        orderId,
                        OutboxEventType.INVENTORY_RESERVED)
                .orElseThrow();

        assertEquals(orderId, outboxEvent.getAggregateId());
        assertEquals(OutboxEventType.INVENTORY_RESERVED, outboxEvent.getType());
        assertNull(outboxEvent.getPublishedAt());
    }

    @Test
    void shouldRollbackAllReservationsWhenAnyItemCannotBeReserved() {

        // GIVEN
        var orderId = UUID.randomUUID();

        var productAId = UUID.randomUUID();
        var productBId = UUID.randomUUID();

        repository.save(InventoryItem.create(productAId, 10));
        repository.save(InventoryItem.create(productBId, 2));

        var itemA = new ReservationItem(productAId, 3);
        var itemB = new ReservationItem(productBId, 5);

        var command = new ReserveInventoryCommand(orderId, List.of(itemA, itemB));

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class, () -> service.reserve(command));

        // THEN
        var productA = repository.findByProductId(productAId).orElseThrow();

        assertEquals(10, productA.getAvailableQuantity());

        var productB = repository.findByProductId(productBId).orElseThrow();

        assertEquals(2, productB.getAvailableQuantity());
    }
}
