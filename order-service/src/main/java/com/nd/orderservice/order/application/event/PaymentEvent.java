package com.nd.orderservice.order.application.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @since 2026
 */
@SuppressWarnings("ALL")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = PaymentSucceededEvent.class,
                name = "PAYMENT_SUCCEEDED"
        ),
        @JsonSubTypes.Type(
                value = PaymentFailedEvent.class,
                name = "PAYMENT_FAILED"
        ),
})
public sealed interface PaymentEvent permits PaymentSucceededEvent, PaymentFailedEvent {
    EventType eventType();
}
