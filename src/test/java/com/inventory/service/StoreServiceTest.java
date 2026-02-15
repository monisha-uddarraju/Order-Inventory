package com.inventory.service;

import com.order.inventory.dto.StoreDTO;
import com.order.inventory.entity.Store;
import com.order.inventory.mapper.StoreMapper;
import com.order.inventory.repository.StoreRepository;
import com.order.inventory.service.StoreService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock private StoreRepository repo;
    @Mock private StoreMapper mapper;

    @InjectMocks
    private StoreService service;

    private Store s1;
    private Store s2;
    private StoreDTO d1;
    private StoreDTO d2;

    @BeforeEach
    void setUp() {
        s1 = Store.builder()
                .id(1)
                .storeName("Main Store")
                .webAddress("https://main.example.com")
                .physicalAddress("Addr 1")
                .build();

        s2 = Store.builder()
                .id(2)
                .storeName("Outlet")
                .webAddress("https://outlet.example.com")
                .physicalAddress("Addr 2")
                .build();

        d1 = StoreDTO.builder()
                .id(1).storeName("Main Store").webAddress("https://main.example.com").physicalAddress("Addr 1")
                .build();

        d2 = StoreDTO.builder()
                .id(2).storeName("Outlet").webAddress("https://outlet.example.com").physicalAddress("Addr 2")
                .build();
    }

    // ---------------------------------------------------------------------
    // all()
    // ---------------------------------------------------------------------

    @Test
    void all_returnsMappedList() {
        when(repo.findAll()).thenReturn(List.of(s1, s2));
        when(mapper.toDto(s1)).thenReturn(d1);
        when(mapper.toDto(s2)).thenReturn(d2);

        List<StoreDTO> out = service.all();

        assertThat(out).containsExactly(d1, d2);
        verify(repo).findAll();
        verify(mapper).toDto(s1);
        verify(mapper).toDto(s2);
        verifyNoMoreInteractions(repo, mapper);
    }

    @Test
    void all_returnsEmpty_whenNoStores() {
        when(repo.findAll()).thenReturn(List.of());

        List<StoreDTO> out = service.all();

        assertThat(out).isEmpty();
        verify(repo).findAll();
        verifyNoInteractions(mapper);
    }
}