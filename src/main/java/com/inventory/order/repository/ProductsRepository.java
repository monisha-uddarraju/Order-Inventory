// com/inventory/order/repository/ProductsRepository.java
package com.inventory.order.repository;

import com.inventory.order.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductsRepository extends JpaRepository<Products, Integer> {
    boolean existsByProductName(String productName);
    List<Products> findByProductNameContainingIgnoreCase(String name);
    List<Products> findByBrandIgnoreCase(String brand);
    List<Products> findByColourIgnoreCase(String colour);
    List<Products> findByUnitPriceBetween(BigDecimal min, BigDecimal max);
}