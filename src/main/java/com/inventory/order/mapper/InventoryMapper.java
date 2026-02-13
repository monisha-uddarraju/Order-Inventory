package com.inventory.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.inventory.order.dto.response.InventoryResponse;
import com.inventory.order.entity.Inventory;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface InventoryMapper {

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "productId", source = "product.id")
    InventoryResponse toResponse(Inventory entity);
}