package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.application.command.ReserveInventoryCommand;
import com.nd.inventoryservice.inventory.domain.exception.InventoryReservationException;
import com.nd.inventoryservice.inventory.messaging.idempotency.ProcessedEvent;
import com.nd.inventoryservice.inventory.messaging.idempotency.ProcessedEventRepository;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEvent;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventFactory;
import com.nd.inventoryservice.inventory.messaging.outbox.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@ExtendWith(MockitoExtension.class)
class InventoryReservationHandlerTest {

    @Captor
    private ArgumentCaptor<ProcessedEvent> processedEventCaptor;

    @Mock
    private InventoryReservationService reservationService;

    @Mock
    private OutboxEventFactory outboxEventFactory;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private InventoryReservationHandler handler;

    @Test
    void shouldReserveInventoryWhenReservationSucceeds() {
        var eventId = UUID.randomUUID();
        var command = new ReserveInventoryCommand(eventId, UUID.randomUUID(), Collections.emptyList());

        handler.handle(command);

        verify(reservationService).reserve(command);
        verifyNoInteractions(outboxEventFactory, outboxEventRepository);
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void shouldSaveFailureOutboxEventWhenReservationFails() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var command = new ReserveInventoryCommand(eventId, orderId, Collections.emptyList());

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
        verify(processedEventRepository).save(processedEventCaptor.capture());

        assertEquals(eventId, processedEventCaptor.getValue().getEventId());
    }
}