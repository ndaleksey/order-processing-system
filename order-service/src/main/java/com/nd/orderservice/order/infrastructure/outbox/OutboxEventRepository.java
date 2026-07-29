package com.nd.orderservice.order.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @since 2026
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop10ByPublishedAtIsNullOrderByCreatedAtAsc();
}
