package com.inventory.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
        @NotNull Integer productId,
        @Min(1) Integer quantity
) {}
//7