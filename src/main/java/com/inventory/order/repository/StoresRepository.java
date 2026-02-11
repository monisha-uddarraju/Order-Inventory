package com.inventory.order.repository;

import com.inventory.order.entity.Stores;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoresRepository extends JpaRepository<Stores, Integer> { 
	
}