package com.nd.paymentservice.payment.provider;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @since 2026
 */
public interface PaymentProvider {
    PaymentResult charge(UUID orderId, BigDecimal amount);
}
