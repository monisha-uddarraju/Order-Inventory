package com.order.inventory.mapper;

import com.order.inventory.dto.ShipmentDTO;
import com.order.inventory.entity.Shipment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    @Mappings({
        @Mapping(source = "store.id", target = "storeId"),
        @Mapping(source = "customer.id", target = "customerId"),
        @Mapping(target = "status", expression = "java(e.getShipmentStatus()!=null ? e.getShipmentStatus().name() : null)")
    })
    ShipmentDTO toDto(Shipment e);
}