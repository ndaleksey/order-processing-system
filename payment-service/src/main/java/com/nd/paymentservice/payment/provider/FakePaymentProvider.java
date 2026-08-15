package com.nd.paymentservice.payment.provider;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @since 2026
 */
@Component
public class FakePaymentProvider implements PaymentProvider {
    @Override
    public PaymentResult charge(UUID orderId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(10_000)) >= 0) {
            return PaymentResult.failed("Insufficient funds");
        }

        return PaymentResult.success();
    }
}
