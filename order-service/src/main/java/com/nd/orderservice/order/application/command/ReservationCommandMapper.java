package com.nd.orderservice.order.application.command;

import com.nd.orderservice.order.messaging.event.InventoryReservationFailedEvent;
import com.nd.orderservice.order.messaging.event.InventoryReservationSucceededEvent;
import org.mapstruct.Mapper;

/**
 * @since 2026
 */
@Mapper(componentModel = "spring")
public interface ReservationCommandMapper {
    InventoryReservationFailedCommand toCommand(InventoryReservationFailedEvent event);

    InventoryReservationSucceededCommand toCommand(InventoryReservationSucceededEvent event);
}
