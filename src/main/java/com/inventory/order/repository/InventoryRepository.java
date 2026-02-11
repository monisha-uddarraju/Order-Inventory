package com.inventory.order.repository;

import com.inventory.order.entity.Inventory;
import com.inventory.order.entity.Products;
import com.inventory.order.entity.Stores;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByStoreAndProduct(Stores store, Products product); 
}