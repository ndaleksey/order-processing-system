package com.nd.orderservice.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID customerId,
        @NotEmpty List<@Valid Item> items
) {
    public record Item(
            @NotNull UUID productId,
            @NotBlank String productName,
            @NotNull @DecimalMin(value = "0.01") BigDecimal productPrice,
            @Min(1) int quantity
    ) {
    }
}
