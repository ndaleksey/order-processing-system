package com.nd.orderservice.order.infrastructure.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @since 2026
 */
@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicsProperties(String orderEvents) {
}
