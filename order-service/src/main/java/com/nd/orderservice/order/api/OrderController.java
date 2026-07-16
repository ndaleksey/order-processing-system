package com.nd.orderservice.order.api;

import com.nd.orderservice.order.application.OrderService;
import com.nd.orderservice.order.api.mapper.CreateOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @since 2026
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CreateOrderMapper mapper;

    @PostMapping
    public CreateOrderResponse create(@RequestBody CreateOrderRequest createOrderRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
