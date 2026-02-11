package com.inventory.order.repository;

import com.inventory.order.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Products, Integer> {
    boolean existsByProductName(String productName);
}