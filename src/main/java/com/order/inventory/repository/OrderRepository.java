package com.order.inventory.repository;

import com.order.inventory.entity.Order;
import com.order.inventory.entity.OrderStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Repository for Orders.
 * Note: Fully-qualified entity names are used in JPQL to avoid any confusion with the 'order' keyword.
 */
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // /api/v1/orders/customer/{customerId}
    @Query("select o from com.order.inventory.entity.Order o where o.customer.id = :custId")
    List<Order> findByCustomerId(@Param("custId") Integer customerId);

    // /api/v1/orders/status/{status}
    List<Order> findByOrderStatus(OrderStatus status);

    // /api/v1/orders/status (status -> count)
    @Query("""
           select o.orderStatus, count(o)
             from com.order.inventory.entity.Order o
            group by o.orderStatus
           """)
    List<Object[]> countOrdersByStatus();

    // /api/v1/orders/date/{startDate}/{endDate}
    @Query("select o from com.order.inventory.entity.Order o where o.orderTms between :start and :end")
    List<Order> findByDateRange(@Param("start") Instant start, @Param("end") Instant end);

    // /api/v1/orders/{store}  (by store name; non-numeric path handled in controller)
    @Query("select o from com.order.inventory.entity.Order o where lower(o.store.storeName) = lower(:storeName)")
    List<Order> findByStoreName(@Param("storeName") String storeName);

    // /api/v1/orders/customer/{email}
    @Query("select o from com.order.inventory.entity.Order o join o.customer c where c.emailAddress = :email")
    List<Order> findByCustomerEmail(@Param("email") String email);
}