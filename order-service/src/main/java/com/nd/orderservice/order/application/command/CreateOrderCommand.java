package com.nd.orderservice.order.application.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * @since 2026
 */
public record CreateOrderCommand(
        UUID customerId,
        List<Item> items
) {
    public record Item(
            UUID productId,
            String name,
            BigDecimal productPrice,
            int quantity
    ) {
    }
}
