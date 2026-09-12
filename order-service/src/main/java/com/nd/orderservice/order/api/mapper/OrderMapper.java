package com.nd.orderservice.order.api.mapper;

import com.nd.orderservice.order.api.CreateOrderRequest;
import com.nd.orderservice.order.api.CreateOrderResponse;
import com.nd.orderservice.order.api.OrderDto;
import com.nd.orderservice.order.application.command.CreateOrderCommand;
import com.nd.orderservice.order.domain.Order;
import com.nd.orderservice.order.domain.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @since 2026
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {
    CreateOrderCommand toCommand(CreateOrderRequest createOrderRequest);

    @Mapping(target = "orderId", source = "id")
    CreateOrderResponse toResponse(Order order);

    OrderDto toDto(Order byId);

    OrderDto.Item toOrderDto(OrderItem item);
}
