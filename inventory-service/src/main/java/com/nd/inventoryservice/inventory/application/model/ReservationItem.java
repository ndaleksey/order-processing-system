package com.nd.inventoryservice.inventory.application.model;

import java.util.UUID;

/**
 * @since 2026
 */
public record ReservationItem(
        UUID productId,
        int quantity
) {
}
