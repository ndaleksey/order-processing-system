package com.nd.orderservice.order.infrastructure.outbox;

/**
 * @since 2026
 */
public enum OutboxEventType {
    ORDER_CREATED,
    INVENTORY_RESERVATION_REQUESTED,
    PAYMENT_COMPENSATION_REQUESTED
}
