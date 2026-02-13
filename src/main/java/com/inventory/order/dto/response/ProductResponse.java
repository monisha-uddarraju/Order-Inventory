
package com.inventory.order.dto.response;

import java.math.BigDecimal;

public record ProductResponse(
        Integer id,
        String productName,
        BigDecimal unitPrice,
        String colour,
        String brand,
        String size,
        Integer rating
) {}