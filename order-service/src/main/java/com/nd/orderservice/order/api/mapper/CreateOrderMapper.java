package com.nd.orderservice.order.api.mapper;

import com.nd.orderservice.order.api.CreateOrderRequest;
import com.nd.orderservice.order.application.command.CreateOrderCommand;
import org.mapstruct.Mapper;

/**
 * @since 2026
 */
@Mapper(componentModel = "spring")
public interface CreateOrderMapper {
    CreateOrderCommand toCommand(CreateOrderRequest createOrderRequest);
}
