package com.order.inventory.repository;

import com.order.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Enforces (store_id, product_id) uniqueness and FK joins used by endpoints.  [1](https://capgemini-my.sharepoint.com/personal/preethi_preethi-reddy_capgemini_com/Documents/Microsoft%20Copilot%20Chat%20Files/script_two.sql)
 */
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    @Query("select i from Inventory i where i.store.id = :storeId")
    List<Inventory> findByStoreId(@Param("storeId") Integer storeId);

    @Query("""
           select i from Inventory i
            where i.product.id = :productId
              and i.store.id = :storeId
           """)
    List<Inventory> findByProductAndStore(@Param("productId") Integer productId,
                                          @Param("storeId") Integer storeId);

    @Query("""
           select distinct inv
             from Inventory inv
             join OrderItem oi
               on oi.product = inv.product
            where oi.shipment is not null
           """)
    List<Inventory> findInventoriesWithAnyShipmentForTheirProduct();
}