
package com.inventory.order.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotNull Integer id,
        @NotBlank String productName,
        @PositiveOrZero BigDecimal unitPrice,
        @NotBlank String colour,
        @NotBlank String brand,
        @NotBlank String size,
        @Min(0) Integer rating
) {}
//11