package com.inventory.order.repository;

import com.inventory.order.entity.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomersRepository extends JpaRepository<Customers, Integer> {
	Optional<Customers> findByEmailAddress(String emailAddress);

	List<Customers> findByFullNameContainingIgnoreCase(String name);

	
	@Query("""
			select s.shipmentStatus as status, count(distinct s.customer.id) as cnt
			from Shipments s
			group by s.shipmentStatus
			""")
	List<Object[]> countCustomersByShipmentStatus();

	
	@Query("""
			select distinct s.customer
			from Shipments s
			where lower(s.shipmentStatus) = lower(:status)
			""")
	List<Customers> findCustomersByShipmentStatus(String status);

}