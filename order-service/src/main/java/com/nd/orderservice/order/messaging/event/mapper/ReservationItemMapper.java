package com.nd.orderservice.order.messaging.event.mapper;

import com.nd.orderservice.order.messaging.event.ReservationItem;
import com.nd.orderservice.order.domain.OrderItem;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @since 2026
 */
@Mapper(componentModel = "spring")
public interface ReservationItemMapper {
    ReservationItem toReservationItem(OrderItem orderItem);

    List<ReservationItem> toReservationItems(List<OrderItem> orderItems);
}
