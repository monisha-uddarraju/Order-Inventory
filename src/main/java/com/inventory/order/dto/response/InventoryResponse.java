
package com.inventory.order.dto.response;

public record InventoryResponse(
        Integer id, Integer storeId, Integer productId, Integer productInventory
) {}