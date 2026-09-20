package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.application.command.ReserveInventoryCommand;
import com.nd.inventoryservice.inventory.application.model.ReservationItem;
import com.nd.inventoryservice.inventory.domain.InventoryItem;
import com.nd.inventoryservice.inventory.domain.InventoryReservationException;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEvent;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventFactory;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventRepository;
import com.nd.inventoryservice.inventory.persistence.InventoryItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@ExtendWith(MockitoExtension.class)
class InventoryReservationServiceTest {

    @Mock
    private OutboxEventFactory eventFactory;

    @Mock
    private OutboxEventRepository eventRepository;

    @Mock
    private InventoryItemRepository repository;

    @InjectMocks
    private InventoryReservationService service;

    @Test
    void shouldReserveRequestedQuantityForExistingInventoryItem() {
        var orderId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var inventoryItem = InventoryItem.create(productId, 10);
        var reservationItem = new ReservationItem(productId, 3);
        var command = new ReserveInventoryCommand(orderId, List.of(reservationItem));
        var eventId = UUID.randomUUID();
        var outboxEvent = OutboxEvent.inventoryReserved(eventId, orderId, Instant.now(), "{}");

        // GIVEN
        when(eventFactory.createInventoryReservationSucceeded(orderId))
                .thenReturn(outboxEvent);

        when(repository.findByProductId(productId))
                .thenReturn(Optional.of(inventoryItem));

        // WHEN
        service.reserve(command);

        // THEN
        assertEquals(7, inventoryItem.getAvailableQuantity());

        verify(eventRepository).save(outboxEvent);
    }

    @Test
    void shouldFailAndNotSaveOutboxEventWhenInventoryItemDoesNotExist() {
        var orderId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var reservationItem = new ReservationItem(productId, 10);
        var command = new ReserveInventoryCommand(orderId, List.of(reservationItem));

        // GIVEN
        when(repository.findByProductId(productId))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(InventoryReservationException.class, () -> service.reserve(command));

        verify(eventRepository, never()).save(any());
    }

}