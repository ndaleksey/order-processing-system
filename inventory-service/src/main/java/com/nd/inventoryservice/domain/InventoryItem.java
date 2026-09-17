package com.nd.inventoryservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;

/**
 * @since 2026
 */
@Entity
@Getter
@Table(name = "inventory_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryItem {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID productId;

    @Column(nullable = false)
    private int availableQuantity;

    private InventoryItem(UUID productId, int availableQuantity) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
    }

    public static InventoryItem create(UUID productId, int availableQuantity) {
        Objects.requireNonNull(productId, "Product id cannot be null");

        if (availableQuantity < 0) {
            throw new IllegalArgumentException("Available quantity cannot be less than zero");
        }

        return new InventoryItem(productId, availableQuantity);
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity cannot be less or equal zero");
        }

        if (quantity > availableQuantity) {
            throw new IllegalArgumentException("Available quantity is less than required quantity");
        }

        availableQuantity -= quantity;
    }
}
