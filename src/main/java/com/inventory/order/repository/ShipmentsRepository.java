package com.inventory.order.repository;

import com.inventory.order.entity.Shipments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentsRepository extends JpaRepository<Shipments, Integer> {
    List<Shipments> findByShipmentStatus(String shipmentStatus);
}