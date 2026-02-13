
package com.inventory.order.repository;

import com.inventory.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    List<Orders> findByOrderStatus(String orderStatus);
    List<Orders> findByCustomer_Id(Integer customerId);
    List<Orders> findByOrderTmsBetween(Instant start, Instant end);
    List<Orders> findByStore_StoreNameIgnoreCase(String storeName);
}//1