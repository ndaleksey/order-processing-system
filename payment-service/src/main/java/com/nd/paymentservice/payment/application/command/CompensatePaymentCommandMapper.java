package com.nd.paymentservice.payment.application.command;

import com.nd.paymentservice.payment.messaging.event.PaymentCompensationRequestedEvent;
import org.mapstruct.Mapper;

/**
 * @since 2026
 */
@Mapper(componentModel = "spring")
public interface CompensatePaymentCommandMapper {
    CompensatePaymentCommand toCommand(PaymentCompensationRequestedEvent event);
}
