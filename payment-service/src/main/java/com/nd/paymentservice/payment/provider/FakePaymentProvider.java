package com.nd.paymentservice.payment.provider;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @since 2026
 */
@Component
public class FakePaymentProvider implements PaymentProvider {
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds";

    @Override
    public PaymentResult charge(UUID orderId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(10_000)) >= 0) {
            return PaymentResult.failed(INSUFFICIENT_FUNDS);
        }

        return PaymentResult.success();
    }
}
