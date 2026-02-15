package com.order.inventory.service;

import com.order.inventory.dto.CustomerDTO;
import com.order.inventory.dto.ShipmentDTO;
import com.order.inventory.entity.Customer;
import com.order.inventory.entity.OrderStatus;
import com.order.inventory.entity.ShipmentStatus;
import com.order.inventory.exception.BadRequestException;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.mapper.CustomerMapper;
import com.order.inventory.repository.CustomerRepository;
import com.order.inventory.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepo;
    private final ShipmentRepository shipmentRepo;
    private final CustomerMapper mapper;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+");

    // ---------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------

    public List<CustomerDTO> all() {
        return customerRepo.findAll().stream().map(mapper::toDto).toList();
    }

    public CustomerDTO create(CustomerDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        // Very light email check to avoid obvious mistakes
        if (!EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            throw new BadRequestException("Invalid email format");
        }
        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new BadRequestException("fullName is required");
        }
        Customer c = mapper.toEntity(dto);
        c.setId(null);
        return mapper.toDto(customerRepo.save(c));
    }

    public CustomerDTO update(Integer id, CustomerDTO dto) {
        Customer c = customerRepo.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
        if (dto.getEmail() != null) {
            if (!EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
                throw new BadRequestException("Invalid email format");
            }
            c.setEmailAddress(dto.getEmail());
        }
        if (dto.getFullName() != null) c.setFullName(dto.getFullName());
        return mapper.toDto(customerRepo.save(c));
    }

    public void delete(Integer id) {
        if (!customerRepo.existsById(id)) throw new NotFoundException("Customer not found");
        customerRepo.deleteById(id);
    }

    // ---------------------------------------------------------
    // Search (email / name)
    // ---------------------------------------------------------

    // Returns 404 if not found (CSV expectation)
    public List<CustomerDTO> byEmailRequired(String email) {
        return customerRepo.findByEmailAddress(email)
                .map(mapper::toDto)
                .map(List::of)
                .orElseThrow(() -> new NotFoundException("Customer with the provided email ID not found."));
    }

    // Returns 404 when empty (CSV expectation)
    public List<CustomerDTO> byNameWildcardRequired(String name) {
        List<CustomerDTO> out = customerRepo.searchByNameWildcard(name).stream().map(mapper::toDto).toList();
        if (out.isEmpty()) {
            throw new NotFoundException("Customer with the provided name wildcard not found.");
        }
        return out;
    }

    // ---------------------------------------------------------
    // Shipment status wise count of customers
    // ---------------------------------------------------------

    public Map<String, Long> shipmentStatusWiseCustomerCount() {
        // [status, distinctCustomers]
        List<Object[]> rows = shipmentRepo.countDistinctCustomersByShipmentStatus();
        return rows.stream().collect(Collectors.toMap(
                r -> String.valueOf(r[0]),
                r -> ((Number) r[1]).longValue()
        ));
    }

    // ---------------------------------------------------------
    // Customer shipments (404 if none)
    // ---------------------------------------------------------

    public List<ShipmentDTO> shipmentsByCustomerRequired(Integer customerId) {
        List<ShipmentDTO> list = shipmentRepo.findByCustomer_Id(customerId)
                .stream()
                .map(s -> ShipmentDTO.builder()
                        .id(s.getId())
                        .storeId(s.getStore().getId())
                        .customerId(s.getCustomer().getId())
                        .deliveryAddress(s.getDeliveryAddress())
                        .status(s.getShipmentStatus() != null ? s.getShipmentStatus().name() : null)
                        .build())
                .toList();
        if (list.isEmpty()) {
            throw new NotFoundException("Shipment history for the specified customer ID not found.");
        }
        return list;
    }

    // ---------------------------------------------------------
    // Derived filters
    // ---------------------------------------------------------

    // Returns 404 when result is empty if required==true; otherwise returns OK with empty list.
    public List<CustomerDTO> customersByShipmentStatus(ShipmentStatus status, boolean required) {
        List<CustomerDTO> out = customerRepo.findCustomersByShipmentStatus(status).stream().map(mapper::toDto).toList();
        if (required && out.isEmpty()) {
            throw new NotFoundException("No customers found with shipment status: " + status);
        }
        return out;
    }

    public List<CustomerDTO> customersWithCompletedOrdersRequired() {
        List<CustomerDTO> out = customerRepo.findCustomersWithOrderStatus(OrderStatus.COMPLETE)
                .stream().map(mapper::toDto).toList();
        if (out.isEmpty()) {
            throw new NotFoundException("No customers found with completed orders.");
        }
        return out;
    }

    public List<CustomerDTO> customersByOrderQuantityBetweenRequired(long min, long max) {
        if (min < 0 || max < 0 || min > max) {
            throw new BadRequestException("Invalid request. Please provide valid minimum and maximum quantities for orders.");
        }
        List<CustomerDTO> out = customerRepo.findCustomersByTotalOrderedQuantityBetween(min, max)
                .stream()
                .map(row -> (Customer) row[0])
                .map(mapper::toDto)
                .toList();
        if (out.isEmpty()) {
            throw new NotFoundException("No customers found in the specified order quantity range.");
        }
        return out;
    }
}