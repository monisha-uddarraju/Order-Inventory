package com.order.inventory.repository;

import com.order.inventory.entity.Customer;
import com.order.inventory.entity.OrderStatus;
import com.order.inventory.entity.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Customer lookups used by Customers API.
 */
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // /api/v1/customers/{emailId}
    Optional<Customer> findByEmailAddress(String emailAddress);

    // /api/v1/customers/name/{name}
    @Query("select c from Customer c where lower(c.fullName) like lower(concat('%', :name, '%'))")
    List<Customer> searchByNameWildcard(@Param("name") String name);

    // /api/v1/customers/shipments/{pending|overdue}
    @Query("""
           select distinct c
             from Customer c
             join Shipment s on s.customer = c
            where s.shipmentStatus = :status
           """)
    List<Customer> findCustomersByShipmentStatus(@Param("status") ShipmentStatus status);

    // /api/v1/customers/orders/completed (and other statuses if needed)
    @Query("""
           select distinct c
             from Customer c
             join com.order.inventory.entity.Order o on o.customer = c
            where o.orderStatus = :status
           """)
    List<Customer> findCustomersWithOrderStatus(@Param("status") OrderStatus status);

    // /api/v1/customers/orders/quantity/{min}/{max}
    @Query("""
           select c, sum(i.quantity)
             from Customer c
             join com.order.inventory.entity.Order o on o.customer = c
             join com.order.inventory.entity.OrderItem i on i.order = o
            group by c
            having sum(i.quantity) between :minQty and :maxQty
           """)
    List<Object[]> findCustomersByTotalOrderedQuantityBetween(@Param("minQty") long minQty,
                                                              @Param("maxQty") long maxQty);
}