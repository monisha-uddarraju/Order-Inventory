
package com.inventory.order.dto.request;

import jakarta.validation.constraints.*;

public record CreateCustomerRequest(
        @Email @NotBlank String emailAddress,
        @NotBlank String fullName
) {}
