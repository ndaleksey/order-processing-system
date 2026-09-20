package com.nd.inventoryservice.inventory.messaging.outbox;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @since 2026
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByPublishedAtIsNullOrderByCreatedAtAsc(Limit limit);

    Optional<OutboxEvent> findByAggregateIdAndTypeAndPublishedAtIsNull(UUID aggregateId, OutboxEventType type);
}
