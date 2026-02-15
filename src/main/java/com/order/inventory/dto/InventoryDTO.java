package com.order.inventory.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryDTO {
    private Integer inventoryId;
    private Integer storeId;
    private String storeName;
    private Integer productId;
    private String productName;
    private Integer quantity;
}