package com.nd.orderservice.order.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 2026
 */
class OrderTest {


    @Test
    void shouldMarkConfirmedWhenStatusIsPaid() {
        // Given
        var order = Order.create(UUID.randomUUID());
        order.markPaid();

        // When
        order.markConfirmed();

        // Then
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void shouldRejectConfirmationWhenStatusIsCreated() {
        var order = Order.create(UUID.randomUUID());

        assertThrows(IllegalStateException.class, order::markConfirmed);
        assertEquals(OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void shouldMarkCanceledWhenStatusIsCreated() {
        // Given
        var order = Order.create(UUID.randomUUID());

        // When
        order.markCanceled();

        // Then
        assertEquals(OrderStatus.CANCELED, order.getStatus());
    }

    @Test
    void shouldMarkCanceledWhenStatusIsPaid() {
        var order = Order.create(UUID.randomUUID());
        order.markPaid();

        order.markCanceled();

        assertEquals(OrderStatus.CANCELED, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenCancelConfirmedOrder() {
        // Given
        var order = Order.create(UUID.randomUUID());

        order.markPaid();

        order.markConfirmed();

        // When-Then
        assertThrows(IllegalStateException.class, order::markCanceled);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenConfirmCanceledOrder() {
        // Given
        var order = Order.create(UUID.randomUUID());

        order.markCanceled();

        // When-Then
        assertThrows(IllegalStateException.class, order::markConfirmed);
        assertEquals(OrderStatus.CANCELED, order.getStatus());
    }
}
