package com.order.inventory.service;

import com.order.inventory.dto.ShipmentDTO;
import com.order.inventory.entity.ShipmentStatus;
import com.order.inventory.mapper.ShipmentMapper;
import com.order.inventory.repository.OrderItemRepository;
import com.order.inventory.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Transactional
public class ShipmentService {
    private final ShipmentRepository repo;
    private final OrderItemRepository itemRepo;
    private final ShipmentMapper mapper;

    public List<ShipmentDTO> byCustomer(Integer customerId) {
        return repo.findByCustomer_Id(customerId).stream().map(mapper::toDto).toList();
    }

    public Map<String, Long> customerCountByStatus() {
        return repo.countDistinctCustomersByShipmentStatus().stream()
                .collect(Collectors.toMap(r -> String.valueOf(r[0]), r -> ((Number) r[1]).longValue()));
    }

    public List<ShipmentDTO.SoldCount> totalSoldGroupedByShipmentStatus() {
        return itemRepo.totalSoldByShipmentStatusAll().stream()
                .map(r -> ShipmentDTO.SoldCount.builder()
                        .status(String.valueOf(r[0]))
                        .totalSold(((Number) r[1]).longValue())
                        .build())
                .toList();
    }

    public List<Integer> customersByStatus(ShipmentStatus status) {
        return repo.findCustomersByShipmentStatus(status).stream().map(c -> c.getId()).toList();
    }
}