package com.inventory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.order.inventory.entity.Customer;
import com.order.inventory.entity.Store;
import com.order.inventory.entity.Shipment;
import com.order.inventory.entity.ShipmentStatus;
import com.order.inventory.dto.CustomerDTO;
import com.order.inventory.dto.ShipmentDTO;
import com.order.inventory.repository.CustomerRepository;
import com.order.inventory.repository.ShipmentRepository;
import com.order.inventory.mapper.CustomerMapper;
import com.order.inventory.service.CustomerService;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock CustomerRepository customerRepo;
    @Mock ShipmentRepository shipmentRepo;
    @Mock CustomerMapper mapper;

    @InjectMocks CustomerService service;

    Customer c1, c2;
    CustomerDTO d1, d2;

    @BeforeEach
    void init() {
        c1 = Customer.builder().id(1)
                .emailAddress("alice@example.com").fullName("Alice").build();

        c2 = Customer.builder().id(2)
                .emailAddress("bob@example.com").fullName("Bob").build();

        d1 = CustomerDTO.builder().id(1)
                .email("alice@example.com").fullName("Alice").build();

        d2 = CustomerDTO.builder().id(2)
                .email("bob@example.com").fullName("Bob").build();
    }

    @Test
    void all_returnsList() {
        when(customerRepo.findAll()).thenReturn(List.of(c1, c2));
        when(mapper.toDto(c1)).thenReturn(d1);
        when(mapper.toDto(c2)).thenReturn(d2);

        List<CustomerDTO> out = service.all();
        assertEquals(2, out.size());
    }

    @Test
    void create_success() {
        CustomerDTO in = CustomerDTO.builder()
                .id(99).email("x@x.com").fullName("X").build();

        Customer mapped = Customer.builder()
                .id(99).emailAddress("x@x.com").fullName("X").build();

        Customer saved = Customer.builder()
                .id(10).emailAddress("x@x.com").fullName("X").build();

        CustomerDTO savedDto = CustomerDTO.builder()
                .id(10).email("x@x.com").fullName("X").build();

        when(mapper.toEntity(in)).thenReturn(mapped);
        when(customerRepo.save(any())).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(savedDto);

        CustomerDTO out = service.create(in);
        assertEquals(10, out.getId());
    }

    @Test
    void update_success() {
        CustomerDTO patch = CustomerDTO.builder()
                .email("new@x.com").fullName("A W").build();

        Customer updated = Customer.builder()
                .id(1).emailAddress("new@x.com").fullName("A W").build();

        CustomerDTO dto = CustomerDTO.builder()
                .id(1).email("new@x.com").fullName("A W").build();

        when(customerRepo.findById(1)).thenReturn(Optional.of(c1));
        when(customerRepo.save(c1)).thenReturn(updated);
        when(mapper.toDto(updated)).thenReturn(dto);

        CustomerDTO out = service.update(1, patch);
        assertEquals("new@x.com", out.getEmail());
    }

    @Test
    void delete_success() {
        when(customerRepo.existsById(1)).thenReturn(true);

        service.delete(1);

        verify(customerRepo).deleteById(1);
    }

    @Test
    void shipmentsByCustomerRequired_maps() {
        Shipment s = mock(Shipment.class);
        Store st = mock(Store.class);

        when(shipmentRepo.findByCustomer_Id(1)).thenReturn(List.of(s));

        when(s.getId()).thenReturn(11);
        when(s.getStore()).thenReturn(st);
        when(st.getId()).thenReturn(22);
        when(s.getCustomer()).thenReturn(c1);
        when(s.getDeliveryAddress()).thenReturn("Addr");
        when(s.getShipmentStatus()).thenReturn(ShipmentStatus.PENDING);

        List<ShipmentDTO> out = service.shipmentsByCustomerRequired(1);
        assertEquals(1, out.size());
    }

    @Test
    void orderQuantity_maps() {
        List<Object[]> rows = Collections.singletonList(
                new Object[]{ c1, 10L }
        );

        when(customerRepo.findCustomersByTotalOrderedQuantityBetween(5, 20))
                .thenReturn(rows);

        when(mapper.toDto(c1)).thenReturn(d1);

        List<CustomerDTO> out =
                service.customersByOrderQuantityBetweenRequired(5, 20);

        assertEquals(1, out.size());
    }
}