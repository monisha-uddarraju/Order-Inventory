package com.inventory.service;

import com.order.inventory.dto.CustomerDTO;
import com.order.inventory.dto.ShipmentDTO;
import com.order.inventory.entity.Customer;
import com.order.inventory.entity.OrderStatus;
import com.order.inventory.entity.Shipment;
import com.order.inventory.entity.ShipmentStatus;
import com.order.inventory.entity.Store;
import com.order.inventory.exception.BadRequestException;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.mapper.CustomerMapper;
import com.order.inventory.repository.CustomerRepository;
import com.order.inventory.repository.ShipmentRepository;
import com.order.inventory.service.CustomerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepo;

    @Mock
    private ShipmentRepository shipmentRepo;

    @Mock
    private CustomerMapper mapper;

    @InjectMocks
    private CustomerService service;

    // ---- Shared fixtures
    private Customer c1;
    private Customer c2;
    private CustomerDTO d1;
    private CustomerDTO d2;

    @BeforeEach
    void setUp() {
        c1 = Customer.builder().id(1).emailAddress("veda@example.com").fullName("Veda Sri").build();
        c2 = Customer.builder().id(2).emailAddress("john@example.com").fullName("John Wick").build();

        d1 = CustomerDTO.builder().id(1).email("veda@example.com").fullName("Veda Sri").build();
        d2 = CustomerDTO.builder().id(2).email("john@example.com").fullName("John Wick").build();
    }

    // ---------------------------------------------------------
    // all()
    // ---------------------------------------------------------
    @Test
    void all_returnsMappedList() {
        when(customerRepo.findAll()).thenReturn(List.of(c1, c2));
        when(mapper.toDto(c1)).thenReturn(d1);
        when(mapper.toDto(c2)).thenReturn(d2);

        List<CustomerDTO> out = service.all();

        assertThat(out).containsExactly(d1, d2);
        verify(customerRepo).findAll();
        verify(mapper).toDto(c1);
        verify(mapper).toDto(c2);
        verifyNoMoreInteractions(customerRepo, mapper);
    }

    // ---------------------------------------------------------
    // create(dto)
    // ---------------------------------------------------------
    @Test
    void create_saves_andReturnsDTO() {
        CustomerDTO input = CustomerDTO.builder().email("new@ex.com").fullName("New User").build();
        Customer toSave = Customer.builder().emailAddress("new@ex.com").fullName("New User").build();
        Customer saved = Customer.builder().id(10).emailAddress("new@ex.com").fullName("New User").build();
        CustomerDTO output = CustomerDTO.builder().id(10).email("new@ex.com").fullName("New User").build();

        when(mapper.toEntity(input)).thenReturn(toSave);
        when(customerRepo.save(toSave)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(output);

        CustomerDTO result = service.create(input);

        assertThat(result).isEqualTo(output);
        // ensure service nulls id before save
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepo).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        verify(mapper).toEntity(input);
        verify(mapper).toDto(saved);
    }

    @Test
    void create_throws_whenEmailMissing() {
        CustomerDTO input = CustomerDTO.builder().email("  ").fullName("X").build();
        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email is required");
        verifyNoInteractions(customerRepo, mapper);
    }

    @Test
    void create_throws_whenInvalidEmail() {
        CustomerDTO input = CustomerDTO.builder().email("invalid-email").fullName("X").build();
        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email format");
        verifyNoInteractions(customerRepo, mapper);
    }

    @Test
    void create_throws_whenFullNameMissing() {
        CustomerDTO input = CustomerDTO.builder().email("x@y.com").fullName(" ").build();
        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("fullName is required");
        verifyNoInteractions(customerRepo, mapper);
    }

    // ---------------------------------------------------------
    // update(id, dto)
    // ---------------------------------------------------------
    @Test
    void update_updatesFields_whenValid() {
        Integer id = 1;
        Customer existing = Customer.builder().id(id).emailAddress("old@ex.com").fullName("Old Name").build();
        CustomerDTO patch = CustomerDTO.builder().email("new@ex.com").fullName("New Name").build();
        Customer saved = Customer.builder().id(id).emailAddress("new@ex.com").fullName("New Name").build();
        CustomerDTO out = CustomerDTO.builder().id(id).email("new@ex.com").fullName("New Name").build();

        when(customerRepo.findById(id)).thenReturn(Optional.of(existing));
        when(customerRepo.save(existing)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(out);

        CustomerDTO result = service.update(id, patch);

        assertThat(result).isEqualTo(out);
        assertThat(existing.getEmailAddress()).isEqualTo("new@ex.com");
        assertThat(existing.getFullName()).isEqualTo("New Name");
        verify(customerRepo).findById(id);
        verify(customerRepo).save(existing);
        verify(mapper).toDto(saved);
    }

    @Test
    void update_throws_whenInvalidEmail() {
        Integer id = 1;
        Customer existing = Customer.builder().id(id).emailAddress("old@ex.com").fullName("Old").build();
        CustomerDTO patch = CustomerDTO.builder().email("bad-email").build();

        when(customerRepo.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(id, patch))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email format");

        verify(customerRepo).findById(id);
        verifyNoMoreInteractions(customerRepo);
        verifyNoInteractions(mapper);
    }

    @Test
    void update_throws_whenNotFound() {
        Integer id = 404;
        when(customerRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, CustomerDTO.builder().build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(customerRepo).findById(id);
        verifyNoMoreInteractions(customerRepo);
        verifyNoInteractions(mapper);
    }

    // ---------------------------------------------------------
    // delete(id)
    // ---------------------------------------------------------
    @Test
    void delete_deletes_whenExists() {
        when(customerRepo.existsById(1)).thenReturn(true);

        service.delete(1);

        verify(customerRepo).existsById(1);
        verify(customerRepo).deleteById(1);
        verifyNoMoreInteractions(customerRepo);
    }

    @Test
    void delete_throws_whenNotFound() {
        when(customerRepo.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(customerRepo).existsById(99);
        verifyNoMoreInteractions(customerRepo);
    }

    // ---------------------------------------------------------
    // byEmailRequired(email)
    // ---------------------------------------------------------
    @Test
    void byEmailRequired_returnsSingleton_whenFound() {
        when(customerRepo.findByEmailAddress("veda@example.com")).thenReturn(Optional.of(c1));
        when(mapper.toDto(c1)).thenReturn(d1);

        List<CustomerDTO> result = service.byEmailRequired("veda@example.com");

        assertThat(result).containsExactly(d1);
        verify(customerRepo).findByEmailAddress("veda@example.com");
        verify(mapper).toDto(c1);
    }

    @Test
    void byEmailRequired_throws_whenNotFound() {
        when(customerRepo.findByEmailAddress("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.byEmailRequired("missing@example.com"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("email ID not found");

        verify(customerRepo).findByEmailAddress("missing@example.com");
        verifyNoInteractions(mapper);
    }

    // ---------------------------------------------------------
    // byNameWildcardRequired(name)
    // ---------------------------------------------------------
    @Test
    void byNameWildcardRequired_returnsList_whenFound() {
        when(customerRepo.searchByNameWildcard("veda")).thenReturn(List.of(c1, c2));
        when(mapper.toDto(c1)).thenReturn(d1);
        when(mapper.toDto(c2)).thenReturn(d2);

        List<CustomerDTO> result = service.byNameWildcardRequired("veda");

        assertThat(result).containsExactly(d1, d2);
        verify(customerRepo).searchByNameWildcard("veda");
    }

    @Test
    void byNameWildcardRequired_throws_whenEmpty() {
        when(customerRepo.searchByNameWildcard("zzz")).thenReturn(List.of());

        assertThatThrownBy(() -> service.byNameWildcardRequired("zzz"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("name wildcard not found");

        verify(customerRepo).searchByNameWildcard("zzz");
        verifyNoInteractions(mapper);
    }

    // ---------------------------------------------------------
    // shipmentStatusWiseCustomerCount()
    // ---------------------------------------------------------
    @Test
    void shipmentStatusWiseCustomerCount_mapsToStringLong() {
        // rows: [ShipmentStatus enum, distinct count]
        List<Object[]> rows = List.of(
                new Object[]{ShipmentStatus.PENDING, 3L},
                new Object[]{ShipmentStatus.DELIVERED, 7}
        );
        when(shipmentRepo.countDistinctCustomersByShipmentStatus()).thenReturn(rows);

        Map<String, Long> result = service.shipmentStatusWiseCustomerCount();

        assertThat(result).containsEntry("PENDING", 3L)
                          .containsEntry("DELIVERED", 7L)
                          .hasSize(2);
        verify(shipmentRepo).countDistinctCustomersByShipmentStatus();
    }

    // ---------------------------------------------------------
    // shipmentsByCustomerRequired(customerId)
    // ---------------------------------------------------------
    @Test
    void shipmentsByCustomerRequired_returnsList_whenFound() {
        // Build minimal graph: Shipment -> Store(id), Customer(id), address, status
        Store store = new Store();
        store.setId(10);
        Customer cust = Customer.builder().id(1).build();

        Shipment s1 = new Shipment();
        s1.setId(1001);
        s1.setStore(store);
        s1.setCustomer(cust);
        s1.setDeliveryAddress("Address 1");
        s1.setShipmentStatus(ShipmentStatus.PENDING);

        Shipment s2 = new Shipment();
        s2.setId(1002);
        s2.setStore(store);
        s2.setCustomer(cust);
        s2.setDeliveryAddress("Address 2");
        s2.setShipmentStatus(null);

        when(shipmentRepo.findByCustomer_Id(1)).thenReturn(List.of(s1, s2));

        List<ShipmentDTO> out = service.shipmentsByCustomerRequired(1);

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getId()).isEqualTo(1001);
        assertThat(out.get(0).getStoreId()).isEqualTo(10);
        assertThat(out.get(0).getCustomerId()).isEqualTo(1);
        assertThat(out.get(0).getDeliveryAddress()).isEqualTo("Address 1");
        assertThat(out.get(0).getStatus()).isEqualTo("PENDING");

        assertThat(out.get(1).getId()).isEqualTo(1002);
        assertThat(out.get(1).getStatus()).isNull();

        verify(shipmentRepo).findByCustomer_Id(1);
    }

    @Test
    void shipmentsByCustomerRequired_throws_whenEmpty() {
        when(shipmentRepo.findByCustomer_Id(1)).thenReturn(List.of());

        assertThatThrownBy(() -> service.shipmentsByCustomerRequired(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Shipment history");

        verify(shipmentRepo).findByCustomer_Id(1);
    }

    // ---------------------------------------------------------
    // customersByShipmentStatus(status, required)
    // ---------------------------------------------------------
    @Nested
    class CustomersByShipmentStatusTests {
        @Test
        void returnsList_whenFound() {
            when(customerRepo.findCustomersByShipmentStatus(ShipmentStatus.PENDING)).thenReturn(List.of(c1));
            when(mapper.toDto(c1)).thenReturn(d1);

            List<CustomerDTO> result = service.customersByShipmentStatus(ShipmentStatus.PENDING, false);

            assertThat(result).containsExactly(d1);
            verify(customerRepo).findCustomersByShipmentStatus(ShipmentStatus.PENDING);
        }

        @Test
        void requiredFalse_returnsEmpty_withoutThrowing() {
            when(customerRepo.findCustomersByShipmentStatus(ShipmentStatus.PENDING)).thenReturn(List.of());

            List<CustomerDTO> result = service.customersByShipmentStatus(ShipmentStatus.PENDING, false);

            assertThat(result).isEmpty();
            verify(customerRepo).findCustomersByShipmentStatus(ShipmentStatus.PENDING);
            verifyNoInteractions(mapper);
        }

        @Test
        void requiredTrue_throws_whenEmpty() {
            when(customerRepo.findCustomersByShipmentStatus(ShipmentStatus.PENDING)).thenReturn(List.of());

            assertThatThrownBy(() -> service.customersByShipmentStatus(ShipmentStatus.PENDING, true))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("No customers found with shipment status");

            verify(customerRepo).findCustomersByShipmentStatus(ShipmentStatus.PENDING);
            verifyNoInteractions(mapper);
        }
    }

    // ---------------------------------------------------------
    // customersWithCompletedOrdersRequired()
    // ---------------------------------------------------------
    @Nested
    class CustomersWithCompletedOrdersRequiredTests {
        @Test
        void returnsList_whenFound() {
            when(customerRepo.findCustomersWithOrderStatus(OrderStatus.COMPLETE)).thenReturn(List.of(c1, c2));
            when(mapper.toDto(c1)).thenReturn(d1);
            when(mapper.toDto(c2)).thenReturn(d2);

            List<CustomerDTO> result = service.customersWithCompletedOrdersRequired();

            assertThat(result).containsExactly(d1, d2);
            verify(customerRepo).findCustomersWithOrderStatus(OrderStatus.COMPLETE);
        }

        @Test
        void throws_whenEmpty() {
            when(customerRepo.findCustomersWithOrderStatus(OrderStatus.COMPLETE)).thenReturn(List.of());

            assertThatThrownBy(service::customersWithCompletedOrdersRequired)
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("completed orders");

            verify(customerRepo).findCustomersWithOrderStatus(OrderStatus.COMPLETE);
            verifyNoInteractions(mapper);
        }
    }

    // ---------------------------------------------------------
    // customersByOrderQuantityBetweenRequired(min, max)
    // ---------------------------------------------------------
    @Nested
    class CustomersByOrderQuantityBetweenRequiredTests {
        @Test
        void throws_whenInvalidRange_negative() {
            assertThatThrownBy(() -> service.customersByOrderQuantityBetweenRequired(-1, 10))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid request");
            verifyNoInteractions(customerRepo, mapper);
        }

        @Test
        void throws_whenInvalidRange_minGreaterThanMax() {
            assertThatThrownBy(() -> service.customersByOrderQuantityBetweenRequired(11, 10))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid request");
            verifyNoInteractions(customerRepo, mapper);
        }

        @Test
        void returnsList_whenNonEmpty() {
            List<Object[]> rows = List.of(new Object[]{c1, 25L}, new Object[]{c2, 40});
            when(customerRepo.findCustomersByTotalOrderedQuantityBetween(10, 50)).thenReturn(rows);
            when(mapper.toDto(c1)).thenReturn(d1);
            when(mapper.toDto(c2)).thenReturn(d2);

            List<CustomerDTO> result = service.customersByOrderQuantityBetweenRequired(10, 50);

            assertThat(result).containsExactly(d1, d2);
            verify(customerRepo).findCustomersByTotalOrderedQuantityBetween(10, 50);
        }

        @Test
        void throws_whenEmpty() {
            when(customerRepo.findCustomersByTotalOrderedQuantityBetween(10, 50)).thenReturn(List.of());

            assertThatThrownBy(() -> service.customersByOrderQuantityBetweenRequired(10, 50))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("quantity range");

            verify(customerRepo).findCustomersByTotalOrderedQuantityBetween(10, 50);
            verifyNoInteractions(mapper);
        }
    }
}