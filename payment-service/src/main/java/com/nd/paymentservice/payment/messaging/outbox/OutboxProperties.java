package com.nd.paymentservice.payment.messaging.outbox;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @since 2026
 */
@Validated
@ConfigurationProperties(prefix = "app.outbox")
public record OutboxProperties(@Positive int batchSize) {
}
