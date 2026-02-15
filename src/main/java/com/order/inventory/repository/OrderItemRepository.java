package com.order.inventory.repository;

import com.order.inventory.entity.OrderItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Matches current dump where order_items has order_id as PK; line_item_id
 * exists but not PK.
 * [1](https://capgemini-my.sharepoint.com/personal/preethi_preethi-reddy_capgemini_com/Documents/Microsoft%20Copilot%20Chat%20Files/script_two.sql)
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

	@Query("select i from OrderItem i where i.order.id = :orderId")
	List<OrderItem> findByOrderId(@Param("orderId") Integer orderId);

	@Query("select coalesce(sum(i.unitPrice * i.quantity), 0) from OrderItem i where i.order.id = :orderId")
	BigDecimal totalAmountByOrderId(@Param("orderId") Integer orderId);

	@Query("""
			select s.shipmentStatus, sum(i.quantity)
			  from OrderItem i
			  join i.shipment s
			 group by s.shipmentStatus
			""")
	List<Object[]> totalSoldByShipmentStatusAll();

	@Query("select i from OrderItem i where i.shipment is not null")
	List<OrderItem> findAllWithShipment();

}