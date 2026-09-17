package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.domain.InventoryItem;
import com.nd.inventoryservice.inventory.persistence.InventoryItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since 2026
 */
@SpringBootTest
@ActiveProfiles("test")
class InventoryReservationServiceIntegrationTest {
    @Autowired
    private InventoryItemRepository repository;

    @Autowired
    private InventoryReservationService service;

    @Test
    void shouldPersistReservedQuantityViaDirtyChecking() {

        //GIVEN
        var productId = UUID.randomUUID();
        var item = InventoryItem.create(productId, 10);

        repository.save(item);

        // WHEN
        service.reserve(productId, 3);

        // THEN
        var savedItem = repository.findByProductId(productId).orElseThrow();
        assertEquals(7, savedItem.getAvailableQuantity());
    }
}
