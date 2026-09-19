package com.nd.inventoryservice.inventory.messaging.consumer;

import com.nd.inventoryservice.inventory.application.InventoryReservationService;
import com.nd.inventoryservice.inventory.application.command.ReserveInventoryCommand;
import com.nd.inventoryservice.inventory.application.mapper.ReservationItemMapper;
import com.nd.inventoryservice.inventory.messaging.event.ReservationItem;
import com.nd.inventoryservice.inventory.messaging.event.InventoryReservationRequestedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@ExtendWith(MockitoExtension.class)
class InventoryReservationRequestedConsumerTest {
    @Captor
    private ArgumentCaptor<ReserveInventoryCommand> commandCaptor;

    @Mock
    private InventoryReservationService reservationService;

    @Mock
    private ReservationItemMapper itemMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private InventoryReservationRequestedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new InventoryReservationRequestedConsumer(objectMapper, reservationService, itemMapper);
    }

    @Test
    void shouldReserveInventoryWhenReservationRequestReceived() {
        var productAId = UUID.randomUUID();
        var productBId = UUID.randomUUID();

        var items = List.of(
                new ReservationItem(productAId, 2),
                new ReservationItem(productBId, 3)
        );

        var orderId = UUID.randomUUID();
        var eventId = UUID.randomUUID();

        var event = new InventoryReservationRequestedEvent(eventId, orderId, items, Instant.now());


        var itemModels = List.of(
                new com.nd.inventoryservice.inventory.application.model.ReservationItem(productAId, 2),
                new com.nd.inventoryservice.inventory.application.model.ReservationItem(productBId, 3)
        );

        when(itemMapper.toReservationItems(items)).thenReturn(itemModels);

        var payload = objectMapper.writeValueAsString(event);

        consumer.consume(payload);

        verify(reservationService).reserve(commandCaptor.capture());

        assertEquals(orderId, commandCaptor.getValue().orderId());
    }
}