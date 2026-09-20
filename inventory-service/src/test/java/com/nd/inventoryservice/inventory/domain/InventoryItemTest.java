package com.nd.inventoryservice.inventory.domain;

import com.nd.inventoryservice.inventory.application.exception.InventoryReservationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 2026
 */
class InventoryItemTest {
    @Test
    void shouldDecreaseAvailableQuantityWhenStockIsReserved() {
        // GIVEN
        var productId = UUID.randomUUID();
        var item = InventoryItem.create(productId, 10);

        // WHEN
        item.reserve(3);

        // THEN
        assertEquals(7, item.getAvailableQuantity());
    }

    @Test
    void shouldRejectReservationWhenQuantityExceedsAvailableStock() {
        // GIVEN
        var productId = UUID.randomUUID();
        var item = InventoryItem.create(productId, 10);

        // WHEN / THEN
        assertThrows(InventoryReservationException.class, () -> item.reserve(20));

        assertEquals(10, item.getAvailableQuantity());
    }

    @Test
    void shouldRejectNegativeReservationQuantity() {
        // GIVEN
        var productId = UUID.randomUUID();
        var item = InventoryItem.create(productId, 10);

        // WHEN / THEN
        assertThrows(InventoryReservationException.class, () -> item.reserve(-10));

        assertEquals(10, item.getAvailableQuantity());
    }

    @Test
    void shouldRejectZeroReservationQuantity() {
        // GIVEN
        var productId = UUID.randomUUID();
        var item = InventoryItem.create(productId, 10);

        // WHEN / THEN
        assertThrows(InventoryReservationException.class, () -> item.reserve(0));

        assertEquals(10, item.getAvailableQuantity());
    }

    @Test
    void shouldRejectNegativeInitialAvailableQuantity() {
        // GIVEN / WHEN / THEN
        var productId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> InventoryItem.create(productId, -10));
    }

    @Test
    void shouldAllowCreatingItemWithZeroAvailableQuantity() {
        // GIVEN / WHEN / THEN
        var productId = UUID.randomUUID();
        assertDoesNotThrow(() -> {
            var item = InventoryItem.create(productId, 0);
            assertEquals(productId, item.getProductId());
            assertEquals(0, item.getAvailableQuantity());
        });

        assertEquals(productId, InventoryItem.create(productId, 0).getProductId());
    }

    @Test
    void shouldRejectNullProductId() {
        // GIVEN / WHEN / THEN
        assertThrows(NullPointerException.class, () -> InventoryItem.create(null, 0));
    }
}
