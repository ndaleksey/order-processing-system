package com.nd.orderservice.order.api;

import com.nd.orderservice.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalPrice,
        List<Item> items,
        Instant createdAt,
        Instant updatedAt
) {
    public record Item(
            UUID productId,
            String name,
            BigDecimal productPrice,
            int quantity
    ) {
    }
}
