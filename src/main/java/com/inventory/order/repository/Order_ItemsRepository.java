
package com.inventory.order.repository;

import com.inventory.order.entity.Order_Items;
import com.inventory.order.entity.Orders;
import com.inventory.order.entity.Order_Items.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface Order_ItemsRepository extends JpaRepository<Order_Items, OrderItemId> {
    List<Order_Items> findByOrder(Orders order);

    
    @Query("""
           select coalesce(oi.shipment.shipmentStatus,'UNASSIGNED') as status,
                  sum(oi.quantity) as totalQty
           from Order_Items oi
           where oi.shipment is not null
           group by oi.shipment.shipmentStatus
           """)
    List<Object[]> sumSoldQuantityByShipmentStatus();
}//1