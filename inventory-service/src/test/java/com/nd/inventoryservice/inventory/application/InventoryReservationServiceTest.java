package com.nd.inventoryservice.inventory.application;

import com.nd.inventoryservice.inventory.domain.InventoryItem;
import com.nd.inventoryservice.inventory.persistence.InventoryItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@ExtendWith(MockitoExtension.class)
class InventoryReservationServiceTest {

    @Mock
    private InventoryItemRepository repository;

    @InjectMocks
    private InventoryReservationService service;

    @Test
    void shouldReserveRequestedQuantityForExistingInventoryItem() {
        var productId = UUID.randomUUID();
        var item = InventoryItem.create(productId, 10);

        // GIVEN
        when(repository.findByProductId(productId))
                .thenReturn(Optional.of(item));

        // WHEN
        service.reserve(productId, 3);

        // THEN
        assertEquals(7, item.getAvailableQuantity());
    }

    @Test
    void shouldFailWhenInventoryItemDoesNotExist() {
        var productId = UUID.randomUUID();

        // GIVEN
        when(repository.findByProductId(productId))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(IllegalStateException.class, () -> service.reserve(productId, 3));
    }

}