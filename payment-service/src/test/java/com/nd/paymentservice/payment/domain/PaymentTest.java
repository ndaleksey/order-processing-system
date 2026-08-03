package com.nd.paymentservice.payment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 2026
 */

class PaymentTest {
    @Test
    void shouldRejectZeroAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Payment.create(UUID.randomUUID(), BigDecimal.ZERO));
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Payment.create(UUID.randomUUID(), BigDecimal.valueOf(-1)));
    }

    @Test
    void shouldCreatePaymentWithCreatedStatus() {
        var payment = Payment.create(UUID.randomUUID(), BigDecimal.valueOf(100L));

        assertEquals(PaymentStatus.CREATED, payment.getStatus());
        assertNotNull(payment.getCreatedAt());
        assertNotNull(payment.getUpdatedAt());
    }
}
