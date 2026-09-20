package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.application.command.ReserveInventoryCommand;
import com.nd.inventoryservice.inventory.application.exception.InventoryReservationException;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEvent;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventFactory;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@ExtendWith(MockitoExtension.class)
class InventoryReservationHandlerTest {
    @Mock
    private InventoryReservationService reservationService;

    @Mock
    private OutboxEventFactory outboxEventFactory;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private InventoryReservationHandler handler;

    @Test
    void shouldReserveInventoryWhenReservationSucceeds() {
        var command = new ReserveInventoryCommand(UUID.randomUUID(), Collections.emptyList());

        handler.handle(command);

        verify(reservationService).reserve(command);
        verifyNoInteractions(outboxEventFactory, outboxEventRepository);
    }

    @Test
    void shouldSaveFailureOutboxEventWhenReservationFails() {
        var orderId = UUID.randomUUID();
        var command = new ReserveInventoryCommand(orderId, Collections.emptyList());

        var exceptionMessage = "Available quantity is less than required quantity";

        var event = OutboxEvent.failedReservation(
                UUID.randomUUID(),
                orderId,
                Instant.now(),
                "{}"
        );

        doThrow(new InventoryReservationException(exceptionMessage))
                .when(reservationService).reserve(any());

        when(outboxEventFactory.createInventoryReservationFailed(orderId, exceptionMessage))
                .thenReturn(event);

        handler.handle(command);

        verify(outboxEventFactory).createInventoryReservationFailed(orderId, exceptionMessage);
        verify(outboxEventRepository).save(event);
    }
}