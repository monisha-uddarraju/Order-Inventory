package com.order.inventory.mapper;

import com.order.inventory.dto.OrderDTO;
import com.order.inventory.dto.OrderItemDTO;
import com.order.inventory.entity.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mappings({
        @Mapping(source = "order.id", target = "orderId"),
        @Mapping(source = "product.id", target = "productId"),
        @Mapping(source = "product.productName", target = "productName"),
        @Mapping(target = "shipmentStatus", expression = "java(e.getShipment()!=null ? e.getShipment().getShipmentStatus().name() : null)")
    })
    OrderItemDTO toDto(OrderItem e);

    @Mappings({
        @Mapping(source = "lineItemId", target = "lineItemId"),
        @Mapping(source = "product.productName", target = "productName"),
        @Mapping(source = "product.id", target = "productId"),
        @Mapping(source = "unitPrice", target = "unitPrice"),
        @Mapping(source = "quantity", target = "quantity"),
        @Mapping(target = "shipmentStatus", expression = "java(e.getShipment()!=null ? e.getShipment().getShipmentStatus().name() : null)")
    })
    OrderDTO.LineItem toLineItem(OrderItem e);
}