package com.nd.orderservice.order.application.exception;

/**
 * @since 2026
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
