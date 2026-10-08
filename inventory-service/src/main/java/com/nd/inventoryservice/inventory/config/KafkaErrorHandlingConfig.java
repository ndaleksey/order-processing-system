package com.nd.inventoryservice.inventory.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * @since 2026
 */
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(
                        record.topic() + ".DLT",
                        record.partition()
                )
        );

        var backoff = new FixedBackOff(
                1000L,
                2L
        );

        var errorHandler = new DefaultErrorHandler(recoverer, backoff);

        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                InvalidFormatException.class
        );

        return errorHandler;
    }
}
