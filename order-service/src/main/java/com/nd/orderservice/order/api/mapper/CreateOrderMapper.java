package com.nd.orderservice.order.api.mapper;

import com.nd.orderservice.order.api.CreateOrderRequest;
import com.nd.orderservice.order.api.CreateOrderResponse;
import com.nd.orderservice.order.application.command.CreateOrderCommand;
import com.nd.orderservice.order.domain.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @since 2026
 */
@Mapper(componentModel = "spring")
public interface CreateOrderMapper {
    CreateOrderCommand toCommand(CreateOrderRequest createOrderRequest);

    @Mapping(target = "orderId", source = "id")
    CreateOrderResponse toResponse(Order order);
}
