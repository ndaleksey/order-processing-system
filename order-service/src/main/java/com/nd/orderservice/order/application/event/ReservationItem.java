package com.nd.orderservice.order.application.event;

import java.util.UUID;

/**
 * @since 2026
 */
public record ReservationItem(
        UUID productId,
        int quantity) {
}
