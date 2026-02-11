package com.inventory.order.repository;

import com.inventory.order.entity.Order_Items;
import com.inventory.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Order_ItemsRepository extends JpaRepository<Order_Items, Integer> {
  
    List<Order_Items> findByOrder(Orders order);
}