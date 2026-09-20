package com.nd.inventoryservice.inventory.messaging.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * @since 2026
 */
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
