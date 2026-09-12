package com.nd.orderservice.order.api;

import com.nd.orderservice.order.api.mapper.OrderMapper;
import com.nd.orderservice.order.application.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @since 2026
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        var command = mapper.toCommand(request);
        var order = orderService.create(command);

        return mapper.toResponse(order);
    }

    @GetMapping("/{id}")
    public OrderDto findById(@PathVariable("id") UUID id) {
        return mapper.toDto(orderService.getById(id));
    }
}
