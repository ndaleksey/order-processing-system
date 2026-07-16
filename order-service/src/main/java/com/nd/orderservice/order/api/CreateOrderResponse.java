package com.nd.orderservice.order.api;

import com.nd.orderservice.order.domain.OrderStatus;

import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId,
        OrderStatus status
) {
}
