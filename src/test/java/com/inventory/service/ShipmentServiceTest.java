package com.inventory.service;
import com.order.inventory.service.ShipmentService;
import com.order.inventory.dto.ShipmentDTO;
import com.order.inventory.entity.Customer;
import com.order.inventory.entity.Shipment;
import com.order.inventory.entity.ShipmentStatus;
import com.order.inventory.mapper.ShipmentMapper;
import com.order.inventory.repository.OrderItemRepository;
import com.order.inventory.repository.ShipmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository repo;

    @Mock
    private OrderItemRepository itemRepo;

    @Mock
    private ShipmentMapper mapper;

    @InjectMocks
    private ShipmentService service;

    // ---------------------------------------------------------
    // byCustomer(Integer customerId)
    // ---------------------------------------------------------
    @Test
    @DisplayName("byCustomer() maps shipments to DTOs")
    void byCustomer_mapsDtos() {
        Integer customerId = 101;

        Shipment s1 = mock(Shipment.class);
        Shipment s2 = mock(Shipment.class);

        ShipmentDTO d1 = ShipmentDTO.builder()
                .id(1).storeId(10).customerId(101).deliveryAddress("Addr-1").status("PENDING").build();
        ShipmentDTO d2 = ShipmentDTO.builder()
                .id(2).storeId(11).customerId(101).deliveryAddress("Addr-2").status("DELIVERED").build();

        when(repo.findByCustomer_Id(customerId)).thenReturn(List.of(s1, s2));
        when(mapper.toDto(s1)).thenReturn(d1);
        when(mapper.toDto(s2)).thenReturn(d2);

        List<ShipmentDTO> out = service.byCustomer(customerId);

        assertEquals(2, out.size());
        assertEquals(1, out.get(0).getId());
        assertEquals(2, out.get(1).getId());
        assertEquals(101, out.get(0).getCustomerId());
        assertEquals("PENDING", out.get(0).getStatus());
        assertEquals("DELIVERED", out.get(1).getStatus());

        verify(repo, times(1)).findByCustomer_Id(customerId);
        verify(mapper, times(1)).toDto(s1);
        verify(mapper, times(1)).toDto(s2);
    }

    // ---------------------------------------------------------
    // customerCountByStatus()
    // ---------------------------------------------------------
    @Test
    @DisplayName("customerCountByStatus() maps rows to status->count")
    void customerCountByStatus_maps() {
        List<Object[]> rows = List.of(
                new Object[]{ShipmentStatus.PENDING, 3L},
                new Object[]{ShipmentStatus.DELIVERED, 7}
        );
        when(repo.countDistinctCustomersByShipmentStatus()).thenReturn(rows);

        Map<String, Long> out = service.customerCountByStatus();

        assertEquals(2, out.size());
        assertEquals(3L, out.get("PENDING"));
        assertEquals(7L, out.get("DELIVERED"));

        verify(repo, times(1)).countDistinctCustomersByShipmentStatus();
    }

    // ---------------------------------------------------------
    // totalSoldGroupedByShipmentStatus()
    // ---------------------------------------------------------
    @Test
    @DisplayName("totalSoldGroupedByShipmentStatus() builds list of SoldCount DTOs")
    void totalSoldGroupedByShipmentStatus_buildsDtoList() {
        List<Object[]> rows = List.of(
                new Object[]{ShipmentStatus.PENDING, 5L},
                new Object[]{ShipmentStatus.DELIVERED, 12}
        );
        when(itemRepo.totalSoldByShipmentStatusAll()).thenReturn(rows);

        List<ShipmentDTO.SoldCount> out = service.totalSoldGroupedByShipmentStatus();

        assertEquals(2, out.size());

        ShipmentDTO.SoldCount c1 = out.get(0);
        assertEquals("PENDING", c1.getStatus());
        assertEquals(5L, c1.getTotalSold());

        ShipmentDTO.SoldCount c2 = out.get(1);
        assertEquals("DELIVERED", c2.getStatus());
        assertEquals(12L, c2.getTotalSold());

        verify(itemRepo, times(1)).totalSoldByShipmentStatusAll();
    }

    // ---------------------------------------------------------
    // customersByStatus(ShipmentStatus)
    // ---------------------------------------------------------
    @Test
    @DisplayName("customersByStatus() collects customer ids from repository")
    void customersByStatus_collectsIds() {
        Customer c1 = new Customer();
        c1.setId(1001);
        Customer c2 = new Customer();
        c2.setId(1002);

        when(repo.findCustomersByShipmentStatus(ShipmentStatus.PENDING)).thenReturn(List.of(c1, c2));

        List<Integer> out = service.customersByStatus(ShipmentStatus.PENDING);

        assertEquals(2, out.size());
        assertEquals(1001, out.get(0));
        assertEquals(1002, out.get(1));

        verify(repo, times(1)).findCustomersByShipmentStatus(ShipmentStatus.PENDING);
    }
}
