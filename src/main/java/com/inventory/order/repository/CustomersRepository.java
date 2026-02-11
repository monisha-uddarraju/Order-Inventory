package com.inventory.order.repository;
 
import com.inventory.order.entity.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
public interface CustomersRepository extends JpaRepository<Customers, Integer> {
    Optional<Customers> findByEmailAddress(String emailAddress);
}