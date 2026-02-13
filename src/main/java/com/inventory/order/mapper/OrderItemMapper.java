package com.inventory.order.mapper;

import org.mapstruct.*;
import com.inventory.order.dto.response.OrderItemResponse;
import com.inventory.order.entity.Order_Items;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderItemMapper {

    @Mappings({
        @Mapping(target = "productId",     source = "product.id"),
        @Mapping(target = "productName",   source = "product.productName"),
        @Mapping(target = "shipmentId",    source = "shipment.id"),
        @Mapping(target = "shipmentStatus",source = "shipment.shipmentStatus")
    })
    OrderItemResponse toResponse(Order_Items entity);

}