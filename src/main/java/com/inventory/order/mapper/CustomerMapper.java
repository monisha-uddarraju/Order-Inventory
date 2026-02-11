package com.inventory.order.mapper;

import org.mapstruct.Mapper;
import com.inventory.order.dto.response.CustomerResponse;
import com.inventory.order.dto.request.CreateCustomerRequest;
import com.inventory.order.entity.Customers;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerResponse toResponse(Customers entity);
    Customers fromCreateRequest(CreateCustomerRequest dto);
}