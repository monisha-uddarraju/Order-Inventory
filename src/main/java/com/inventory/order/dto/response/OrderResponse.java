// com/inventory/order/dto/response/OrderResponse.java
package com.inventory.order.dto.response;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Integer id,
        Instant orderTms,
        String orderStatus,
        Integer customerId,
        Integer storeId,
        List<OrderItemResponse> items
) {}