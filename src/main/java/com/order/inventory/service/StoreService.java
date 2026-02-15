package com.order.inventory.service;
 
import com.order.inventory.dto.StoreDTO;

import com.order.inventory.mapper.StoreMapper;

import com.order.inventory.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
 
@Service @RequiredArgsConstructor @Transactional

public class StoreService {

    private final StoreRepository repo;

    private final StoreMapper mapper;
 
    public List<StoreDTO> all() {

        return repo.findAll().stream().map(mapper::toDto).toList();

    }

}
 