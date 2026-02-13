package com.inventory.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.inventory.order.dto.request.CreateCustomerRequest;
import com.inventory.order.dto.response.CustomerResponse;
import com.inventory.order.entity.Customers;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CustomerMapper {

    // Entity -> DTO
    CustomerResponse toResponse(Customers entity);

    // Create DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "shipments", ignore = true)
    Customers fromCreateRequest(CreateCustomerRequest dto);
}
//1