// com/inventory/order/dto/response/OrderItemResponse.java
package com.inventory.order.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Integer lineItemId,
        Integer productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        Integer shipmentId,
        String shipmentStatus
) {}