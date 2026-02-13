
package com.inventory.order.dto.response;

public record ShipmentSoldSummaryResponse(
        String shipmentStatus, Long totalQuantity
) {}
//19