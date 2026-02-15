package com.order.inventory.repository;

import com.order.inventory.entity.Customer;
import com.order.inventory.entity.Shipment;
import com.order.inventory.entity.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Shipment lookups for customer shipment history and aggregates.
 */
public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {

    // /api/v1/customers/{custId}/shipment
    List<Shipment> findByCustomer_Id(Integer customerId);

    // /api/v1/customers/shipment/status
    @Query("""
           select s.shipmentStatus, count(distinct s.customer.id)
             from Shipment s
            group by s.shipmentStatus
           """)
    List<Object[]> countDistinctCustomersByShipmentStatus();

    // helper for /customers/shipments/{pending|overdue}
    @Query("select distinct s.customer from Shipment s where s.shipmentStatus = :status")
    List<Customer> findCustomersByShipmentStatus(@Param("status") ShipmentStatus status);
}