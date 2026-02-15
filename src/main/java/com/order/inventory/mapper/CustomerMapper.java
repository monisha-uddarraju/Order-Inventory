package com.order.inventory.mapper;

import com.order.inventory.dto.CustomerDTO;
import com.order.inventory.entity.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(source = "emailAddress", target = "email")
    CustomerDTO toDto(Customer e);

    @InheritInverseConfiguration
    Customer toEntity(CustomerDTO dto);
}