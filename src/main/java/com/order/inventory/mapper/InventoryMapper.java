package com.order.inventory.mapper;

import com.order.inventory.dto.InventoryDTO;
import com.order.inventory.entity.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mappings({
        @Mapping(source = "id", target = "inventoryId"),
        @Mapping(source = "store.id", target = "storeId"),
        @Mapping(source = "store.storeName", target = "storeName"),
        @Mapping(source = "product.id", target = "productId"),
        @Mapping(source = "product.productName", target = "productName"),
        @Mapping(source = "productInventory", target = "quantity")
    })
    InventoryDTO toDto(Inventory e);
}