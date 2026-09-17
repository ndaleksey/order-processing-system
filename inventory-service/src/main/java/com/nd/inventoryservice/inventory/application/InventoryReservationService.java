package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.persistence.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @since 2026
 */
@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final InventoryItemRepository repository;

    @Transactional
    public void reserve(UUID productId, int quantity) {
        var item = repository.findByProductId(productId).orElseThrow();

        item.reserve(quantity);
    }
}
