package com.inventory.service;
import com.order.inventory.service.StoreService;
import com.order.inventory.dto.StoreDTO;
import com.order.inventory.entity.Store;
import com.order.inventory.mapper.StoreMapper;
import com.order.inventory.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository repo;

    @Mock
    private StoreMapper mapper;

    @InjectMocks
    private StoreService service;

    @Test
    @DisplayName("all() returns mapped list when repository has stores")
    void all_returnsMappedList() {
        Store s1 = new Store();
        s1.setId(1);
        s1.setStoreName("Main Store");
        s1.setWebAddress("https://main.example.com");
        s1.setPhysicalAddress("123 Main St");

        Store s2 = new Store();
        s2.setId(2);
        s2.setStoreName("Outlet Store");
        s2.setWebAddress("https://outlet.example.com");
        s2.setPhysicalAddress("456 Outlet Ave");

        StoreDTO d1 = StoreDTO.builder()
                .id(1).storeName("Main Store").webAddress("https://main.example.com").physicalAddress("123 Main St")
                .build();
        StoreDTO d2 = StoreDTO.builder()
                .id(2).storeName("Outlet Store").webAddress("https://outlet.example.com").physicalAddress("456 Outlet Ave")
                .build();

        when(repo.findAll()).thenReturn(List.of(s1, s2));
        when(mapper.toDto(s1)).thenReturn(d1);
        when(mapper.toDto(s2)).thenReturn(d2);

        List<StoreDTO> out = service.all();

        assertEquals(2, out.size());
        assertEquals(1, out.get(0).getId());
        assertEquals("Main Store", out.get(0).getStoreName());
        assertEquals(2, out.get(1).getId());
        assertEquals("Outlet Store", out.get(1).getStoreName());

        verify(repo, times(1)).findAll();
        verify(mapper, times(1)).toDto(s1);
        verify(mapper, times(1)).toDto(s2);
    }

    @Test
    @DisplayName("all() returns empty list when repository is empty")
    void all_returnsEmptyList() {
        when(repo.findAll()).thenReturn(List.of());

        List<StoreDTO> out = service.all();

        assertTrue(out.isEmpty());
        verify(repo, times(1)).findAll();
        verify(mapper, never()).toDto(any());
    }
}