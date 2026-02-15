package com.order.inventory.service;

import com.order.inventory.dto.OrderDTO;
import com.order.inventory.dto.OrderDTO.LineItem;
import com.order.inventory.entity.Customer;
import com.order.inventory.entity.Order;
import com.order.inventory.entity.OrderStatus;
import com.order.inventory.entity.Store;
import com.order.inventory.exception.BadRequestException;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.mapper.OrderItemMapper;
import com.order.inventory.mapper.OrderMapper;
import com.order.inventory.repository.CustomerRepository;
import com.order.inventory.repository.OrderItemRepository;
import com.order.inventory.repository.OrderRepository;
import com.order.inventory.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final CustomerRepository customerRepo;
    private final StoreRepository storeRepo;
    private final OrderMapper orderMapper;
    private final OrderItemMapper itemMapper;

    // ---------------------------------------------------------------------
    // Basic CRUD / Reads
    // ---------------------------------------------------------------------

    public List<OrderDTO> all() {
        return orderRepo.findAll().stream().map(orderMapper::toDto).toList();
    }

    public OrderDTO get(Integer id) {
        return orderRepo.findById(id).map(orderMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    public OrderDTO create(OrderDTO dto) {
        if (dto.getCustomerId() == null || dto.getStoreId() == null || dto.getStatus() == null)
            throw new BadRequestException("customerId, storeId and status are required");

        Customer c = customerRepo.findById(dto.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        Store s = storeRepo.findById(dto.getStoreId())
                .orElseThrow(() -> new NotFoundException("Store not found"));

        Order o = new Order();
        o.setCustomer(c);
        o.setStore(s);
        try {
            o.setOrderStatus(OrderStatus.valueOf(dto.getStatus().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid order status");
        }
        o.setOrderTms(dto.getOrderTms() != null ? dto.getOrderTms() : Instant.now());
        return orderMapper.toDto(orderRepo.save(o));
    }

    public OrderDTO update(Integer id, OrderDTO dto) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        if (dto.getStatus() != null) {
            try {
                o.setOrderStatus(OrderStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid order status");
            }
        }
        if (dto.getOrderTms() != null) {
            o.setOrderTms(dto.getOrderTms());
        }
        // (Optional) allow re-assignment of store/customer if provided
        if (dto.getCustomerId() != null) {
            Customer c = customerRepo.findById(dto.getCustomerId())
                    .orElseThrow(() -> new NotFoundException("Customer not found"));
            o.setCustomer(c);
        }
        if (dto.getStoreId() != null) {
            Store s = storeRepo.findById(dto.getStoreId())
                    .orElseThrow(() -> new NotFoundException("Store not found"));
            o.setStore(s);
        }
        return orderMapper.toDto(orderRepo.save(o));
    }

    public void delete(Integer id) {
        if (!orderRepo.existsById(id)) throw new NotFoundException("Order not found");
        orderRepo.deleteById(id);
    }

    // ---------------------------------------------------------------------
    // Aggregations / Filters
    // ---------------------------------------------------------------------

    public Map<String, Long> countByStatus() {
        return orderRepo.countOrdersByStatus().stream()
                .collect(Collectors.toMap(r -> String.valueOf(r[0]), r -> ((Number) r[1]).longValue()));
    }

    public List<OrderDTO> byStatus(String status) {
        OrderStatus st;
        try {
            st = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status");
        }
        return orderRepo.findByOrderStatus(st).stream().map(orderMapper::toDto).toList();
    }

    public List<OrderDTO> byCustomer(Integer customerId) {
        return orderRepo.findByCustomerId(customerId).stream().map(orderMapper::toDto).toList();
    }

    /**
     * Helper required by Customers CSV: return 404 when a customer has no orders.
     * Used by CustomerController -> GET /api/v1/customers/{custId}/order
     */
    public List<OrderDTO> byCustomerRequired(Integer customerId) {
        List<OrderDTO> out = byCustomer(customerId);
        if (out.isEmpty()) {
            throw new NotFoundException("Orders for the specified customer ID not found.");
        }
        return out;
    }

    public List<OrderDTO> byCustomerEmail(String email) {
        return orderRepo.findByCustomerEmail(email).stream().map(orderMapper::toDto).toList();
    }

    public List<OrderDTO> byStoreName(String storeName) {
        return orderRepo.findByStoreName(storeName).stream().map(orderMapper::toDto).toList();
    }

    public List<OrderDTO> byDateRange(String startDate, String endDate) {
        try {
            Instant start = LocalDate.parse(startDate).atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant end = LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZoneOffset.UTC).minusSeconds(1).toInstant();
            if (start.isAfter(end)) throw new BadRequestException("startDate must be <= endDate");
            return orderRepo.findByDateRange(start, end).stream().map(orderMapper::toDto).toList();
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Dates must be yyyy-MM-dd");
        }
    }

    // ---------------------------------------------------------------------
    // Details / Cancel
    // ---------------------------------------------------------------------

    public OrderDTO.Details details(Integer orderId) {
        Order o = orderRepo.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        List<LineItem> items = itemRepo.findByOrderId(orderId).stream().map(itemMapper::toLineItem).toList();
        BigDecimal total = itemRepo.totalAmountByOrderId(orderId);
        return OrderDTO.Details.builder()
                .orderId(orderId)
                .storeName(o.getStore().getStoreName())
                .shipmentStatus(items.stream()
                        .map(LineItem::getShipmentStatus)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null))
                .items(items)
                .totalAmount(total)
                .build();
    }

    public OrderDTO cancel(Integer id) {
        Order o = orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        o.setOrderStatus(OrderStatus.CANCELLED);
        return orderMapper.toDto(orderRepo.save(o));
    }
}