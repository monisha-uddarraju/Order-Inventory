package com.order.inventory.service;

import com.order.inventory.dto.OrderItemDTO;
import com.order.inventory.mapper.OrderItemMapper;
import com.order.inventory.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class OrderItemService {
    private final OrderItemRepository repo;
    private final OrderItemMapper mapper;

    public List<OrderItemDTO> byOrder(Integer orderId) {
        return repo.findByOrderId(orderId).stream().map(mapper::toDto).toList();
    }
}