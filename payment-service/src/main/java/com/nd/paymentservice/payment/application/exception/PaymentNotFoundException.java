package com.nd.paymentservice.payment.application.exception;

/**
 * @since 2026
 */
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
