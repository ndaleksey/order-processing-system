package com.nd.inventoryservice.inventory.application.command;

import com.nd.inventoryservice.inventory.application.model.ReservationItem;

import java.util.List;
import java.util.UUID;

/**
 * @since 2026
 */
public record ReserveInventoryCommand(
        UUID orderId,
        List<ReservationItem> items
) {
}
