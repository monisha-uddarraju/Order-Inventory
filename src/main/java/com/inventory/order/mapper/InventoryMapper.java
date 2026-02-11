package com.inventory.order.mapper;

import org.mapstruct.Mapper;
import com.inventory.order.dto.response.InventoryResponse;
import com.inventory.order.entity.Inventory;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    InventoryResponse toResponse(Inventory entity);
}