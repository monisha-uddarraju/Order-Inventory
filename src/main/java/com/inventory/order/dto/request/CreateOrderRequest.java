
package com.inventory.order.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record CreateOrderRequest(
        @NotNull Integer customerId,
        @NotNull Integer storeId,
        @NotEmpty List<CreateOrderItemRequest> items
) {}

//8