package com.inventory.service;

import com.order.inventory.dto.ShipmentDTO;
import com.order.inventory.entity.Customer;
import com.order.inventory.entity.Shipment;
import com.order.inventory.entity.ShipmentStatus;
import com.order.inventory.entity.Store;
import com.order.inventory.mapper.ShipmentMapper;
import com.order.inventory.repository.OrderItemRepository;
import com.order.inventory.repository.ShipmentRepository;
import com.order.inventory.service.ShipmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock private ShipmentRepository repo;
    @Mock private OrderItemRepository itemRepo;
    @Mock private ShipmentMapper mapper;

    @InjectMocks
    private ShipmentService service;

    // ---- fixtures
    private Store store;
    private Customer cust1;
    private Customer cust2;
    private Shipment s1;
    private Shipment s2;
    private ShipmentDTO d1;
    private ShipmentDTO d2;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(10);

        cust1 = Customer.builder().id(100).build();
        cust2 = Customer.builder().id(200).build();

        s1 = new Shipment();
        s1.setId(1001);
        s1.setStore(store);
        s1.setCustomer(cust1);
        s1.setDeliveryAddress("Addr 1");
        s1.setShipmentStatus(ShipmentStatus.PENDING);

        s2 = new Shipment();
        s2.setId(1002);
        s2.setStore(store);
        s2.setCustomer(cust1);
        s2.setDeliveryAddress("Addr 2");
        s2.setShipmentStatus(ShipmentStatus.DELIVERED);

        d1 = ShipmentDTO.builder()
                .id(1001)
                .storeId(10)
                .customerId(100)
                .deliveryAddress("Addr 1")
                .status("PENDING")
                .build();

        d2 = ShipmentDTO.builder()
                .id(1002)
                .storeId(10)
                .customerId(100)
                .deliveryAddress("Addr 2")
                .status("DELIVERED")
                .build();
    }

    // ---------------------------------------------------------------------
    // byCustomer(customerId)
    // ---------------------------------------------------------------------

    @Test
    void byCustomer_returnsMappedList() {
        when(repo.findByCustomer_Id(100)).thenReturn(List.of(s1, s2));
        when(mapper.toDto(s1)).thenReturn(d1);
        when(mapper.toDto(s2)).thenReturn(d2);

        List<ShipmentDTO> out = service.byCustomer(100);

        assertThat(out).containsExactly(d1, d2);
        verify(repo).findByCustomer_Id(100);
        verify(mapper).toDto(s1);
        verify(mapper).toDto(s2);
        verifyNoMoreInteractions(repo, mapper);
    }

    @Test
    void byCustomer_returnsEmpty_whenNoShipments() {
        when(repo.findByCustomer_Id(999)).thenReturn(List.of());

        List<ShipmentDTO> out = service.byCustomer(999);

        assertThat(out).isEmpty();
        verify(repo).findByCustomer_Id(999);
        verifyNoInteractions(mapper);
    }

    // ---------------------------------------------------------------------
    // customerCountByStatus()
    // ---------------------------------------------------------------------

    @Test
    void customerCountByStatus_mapsEnumKeysToStringAndCountsToLong() {
        List<Object[]> rows = List.of(
                new Object[]{ShipmentStatus.PENDING, 3L},
                new Object[]{ShipmentStatus.DELIVERED, 5}  // int -> long
        );
        when(repo.countDistinctCustomersByShipmentStatus()).thenReturn(rows);

        Map<String, Long> out = service.customerCountByStatus();

        assertThat(out).containsEntry("PENDING", 3L)
                       .containsEntry("DELIVERED", 5L)
                       .hasSize(2);
        verify(repo).countDistinctCustomersByShipmentStatus();
        verifyNoInteractions(itemRepo, mapper);
    }

    // ---------------------------------------------------------------------
    // totalSoldGroupedByShipmentStatus()
    // ---------------------------------------------------------------------

    @Test
    void totalSoldGroupedByShipmentStatus_mapsToDtoList() {
        List<Object[]> rows = List.of(
                new Object[]{ShipmentStatus.PENDING, 12L},
                new Object[]{ShipmentStatus.DELIVERED, 34} // int -> long
        );
        when(itemRepo.totalSoldByShipmentStatusAll()).thenReturn(rows);

        List<ShipmentDTO.SoldCount> out = service.totalSoldGroupedByShipmentStatus();

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(out.get(0).getTotalSold()).isEqualTo(12L);
        assertThat(out.get(1).getStatus()).isEqualTo("DELIVERED");
        assertThat(out.get(1).getTotalSold()).isEqualTo(34L);

        verify(itemRepo).totalSoldByShipmentStatusAll();
        verifyNoInteractions(repo, mapper);
    }

    // ---------------------------------------------------------------------
    // customersByStatus(status)
    // ---------------------------------------------------------------------

    @Test
    void customersByStatus_returnsCustomerIds() {
        when(repo.findCustomersByShipmentStatus(ShipmentStatus.PENDING)).thenReturn(List.of(cust1, cust2));

        List<Integer> out = service.customersByStatus(ShipmentStatus.PENDING);

        assertThat(out).containsExactly(100, 200);
        verify(repo).findCustomersByShipmentStatus(ShipmentStatus.PENDING);
        verifyNoInteractions(itemRepo, mapper);
    }
}
