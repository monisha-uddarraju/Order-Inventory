package com.order.inventory.repository;

import com.order.inventory.entity.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product lookups used by Products API.
 */
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    List<Product> findByBrandIgnoreCase(String brand);

    List<Product> findByColourIgnoreCase(String colour);

    @Query("select p from Product p where p.unitPrice between :min and :max")
    List<Product> findByUnitPriceBetween(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    @Query("select p from Product p where lower(p.productName) like lower(concat('%', :name, '%'))")
    List<Product> searchByName(@Param("name") String name);
}