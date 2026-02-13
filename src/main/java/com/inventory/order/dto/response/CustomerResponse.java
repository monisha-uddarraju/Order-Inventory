
package com.inventory.order.dto.response;

public record CustomerResponse(
        Integer id,
        String emailAddress,
        String fullName
) {}