package com.nd.orderservice.order.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * @since 2026
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    @UuidGenerator
    private UUID id;

    private String type;

    private UUID aggregateId;

    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    public static OutboxEvent orderCreated(UUID orderId, String payload) {
        var event = new OutboxEvent();
        event.type = "ORDER_CREATED";
        event.aggregateId = orderId;
        event.createdAt = Instant.now();
        event.payload = payload;
        return event;
    }
}
