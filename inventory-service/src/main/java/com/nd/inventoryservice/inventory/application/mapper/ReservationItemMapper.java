package com.nd.inventoryservice.inventory.application.mapper;

import com.nd.inventoryservice.inventory.application.model.ReservationItem;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @since 2026
 */
@Mapper(componentModel = "spring")
public interface ReservationItemMapper {

    ReservationItem toReservationItem(com.nd.inventoryservice.inventory.messaging.event.ReservationItem item);

    List<ReservationItem> toReservationItems(List<com.nd.inventoryservice.inventory.messaging.event.ReservationItem> items);
}
