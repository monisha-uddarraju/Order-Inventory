package com.inventory.order.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private Integer id;
    private Integer storeId;
    private Integer productId;
    private Integer productInventory;
}
//13