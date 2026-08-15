package com.nd.paymentservice.payment.provider;

/**
 * @since 2026
 */
public record PaymentResult(
        boolean successful,
        String failureReason
) {
    public static PaymentResult success() {
        return new PaymentResult(true, "Success");
    }

    public static PaymentResult failed(String reason) {
        return new PaymentResult(false, reason);
    }
}
