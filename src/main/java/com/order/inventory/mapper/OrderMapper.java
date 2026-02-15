package com.order.inventory.mapper;

import com.order.inventory.dto.OrderDTO;
import com.order.inventory.entity.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { OrderItemMapper.class })
public interface OrderMapper {

    @Mappings({
        @Mapping(source = "customer.id", target = "customerId"),
        @Mapping(source = "store.id",     target = "storeId"),
        @Mapping(target = "status", expression = "java(e.getOrderStatus()!=null ? e.getOrderStatus().name() : null)"),

        // NEW: map store fields
        @Mapping(source = "store.storeName", target = "storeName"),
        @Mapping(source = "store.webAddress", target = "webAddress")
    })
    OrderDTO toDto(Order e);
}