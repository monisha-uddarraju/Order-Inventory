
package com.inventory.order.dto.request;

import jakarta.validation.constraints.*;

public record UpdateCustomerRequest(
        @NotNull Integer id,
        @Email @NotBlank String emailAddress,
        @NotBlank String fullName
) {}
//10