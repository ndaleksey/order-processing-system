package com.nd.orderservice.order.infrastructure.kafka;

import com.nd.orderservice.order.application.OrderInventoryReservationResultService;
import com.nd.orderservice.order.application.command.ReservationCommandMapper;
import com.nd.orderservice.order.messaging.event.InventoryReservationEvent;
import com.nd.orderservice.order.messaging.event.InventoryReservationFailedEvent;
import com.nd.orderservice.order.messaging.event.InventoryReservationSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class InventoryReservationResultConsumer {

    private final ObjectMapper objectMapper;
    private final OrderInventoryReservationResultService reservationResultService;
    private final ReservationCommandMapper reservationCommandMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.inventory-results}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String payload) {
        var event = objectMapper.readValue(payload, InventoryReservationEvent.class);

        switch (event) {
            case InventoryReservationSucceededEvent e ->
                    reservationResultService.handle(reservationCommandMapper.toCommand(e));
            case InventoryReservationFailedEvent e ->
                    reservationResultService.handle(reservationCommandMapper.toCommand(e));
        }
    }
}
