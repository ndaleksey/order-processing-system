package com.nd.orderservice.order.messaging.event;

/**
 * @since 2026
 */
public enum EventType {
    PAYMENT_SUCCEEDED,
    PAYMENT_FAILED,
    PAYMENT_COMPENSATED,
    RESERVATION_SUCCEEDED,
    RESERVATION_FAILED
}
