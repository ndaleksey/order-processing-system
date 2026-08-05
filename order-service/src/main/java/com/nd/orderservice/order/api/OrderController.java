package com.nd.orderservice.order.api;

import com.nd.orderservice.order.application.OrderService;
import com.nd.orderservice.order.api.mapper.CreateOrderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @since 2026
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CreateOrderMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        var command = mapper.toCommand(request);
        var order = orderService.create(command);

        return mapper.toResponse(order);
    }
}
