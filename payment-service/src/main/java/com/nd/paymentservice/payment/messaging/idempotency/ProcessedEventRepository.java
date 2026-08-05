package com.nd.paymentservice.payment.messaging.idempotency;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

/**
 * @since 2026
 */
public interface ProcessedEventRepository extends CrudRepository<ProcessedEvent, UUID> {
}
