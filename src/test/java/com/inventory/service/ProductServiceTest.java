package com.inventory.service;
import com.order.inventory.service.*;
import com.order.inventory.dto.ProductDTO;
import com.order.inventory.entity.Product;
import com.order.inventory.exception.BadRequestException;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.mapper.ProductMapper;
import com.order.inventory.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repo;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductService service;

    // ---------------------------------------------------------------------
    // getAll(String sortField)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("getAll() returns unsorted when sortField is null/blank")
    void getAll_unsortedWhenNullOrBlank() {
        // null case
        when(repo.findAll(Sort.unsorted())).thenReturn(List.of());
        List<ProductDTO> out1 = service.getAll(null);
        assertTrue(out1.isEmpty());
        verify(repo, times(1)).findAll(Sort.unsorted());

        // blank case
        reset(repo);
        when(repo.findAll(Sort.unsorted())).thenReturn(List.of());
        List<ProductDTO> out2 = service.getAll("  ");
        assertTrue(out2.isEmpty());
        verify(repo, times(1)).findAll(Sort.unsorted());
    }

    @Test
    @DisplayName("getAll() applies valid sort field (ascending)")
    void getAll_validSortApplied() {
        Product p = new Product(); p.setId(1); p.setProductName("A");
        ProductDTO d = ProductDTO.builder().id(1).name("A").build();

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        when(repo.findAll(any(Sort.class))).thenReturn(List.of(p));
        when(mapper.toDto(p)).thenReturn(d);

        List<ProductDTO> out = service.getAll("unitPrice");

        assertEquals(1, out.size());
        verify(repo).findAll(sortCaptor.capture());
        Sort sort = sortCaptor.getValue();
        assertTrue(sort.isSorted());
        assertNotNull(sort.getOrderFor("unitPrice"));
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("unitPrice").getDirection());
    }

    @Test
    @DisplayName("getAll() rejects invalid sort field")
    void getAll_invalidSortField() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.getAll("unknownField"));
        assertTrue(ex.getMessage().startsWith("Invalid sort field."));
        verify(repo, never()).findAll(any(Sort.class));
    }

    // ---------------------------------------------------------------------
    // getAllStrict(String field)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("getAllStrict() applies valid field")
    void getAllStrict_valid() {
        Product p = new Product(); p.setId(2); p.setProductName("B");
        ProductDTO d = ProductDTO.builder().id(2).name("B").build();

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

        when(repo.findAll(any(Sort.class))).thenReturn(List.of(p));
        when(mapper.toDto(p)).thenReturn(d);

        List<ProductDTO> out = service.getAllStrict("brand");

        assertEquals(1, out.size());
        verify(repo).findAll(sortCaptor.capture());
        Sort sort = sortCaptor.getValue();
        assertTrue(sort.isSorted());
        assertNotNull(sort.getOrderFor("brand"));
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("brand").getDirection());
    }

    @Test
    @DisplayName("getAllStrict() rejects null/blank/invalid field")
    void getAllStrict_invalid() {
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> service.getAllStrict(null));
        assertTrue(ex1.getMessage().startsWith("Invalid sort field."));

        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> service.getAllStrict("  "));
        assertTrue(ex2.getMessage().startsWith("Invalid sort field."));

        BadRequestException ex3 = assertThrows(BadRequestException.class, () -> service.getAllStrict("oops"));
        assertTrue(ex3.getMessage().startsWith("Invalid sort field."));
    }

    // ---------------------------------------------------------------------
    // create(ProductDTO dto)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("create() succeeds with valid name and non-negative price")
    void create_success() {
        ProductDTO input = ProductDTO.builder()
                .id(100)
                .name("Phone")
                .unitPrice(new BigDecimal("499.99"))
                .brand("X")
                .colour("Black")
                .size("M")
                .rating(4)
                .build();

        Product mapped = new Product();
        mapped.setId(100);
        mapped.setProductName("Phone");
        mapped.setUnitPrice(new BigDecimal("499.99"));
        mapped.setBrand("X");
        mapped.setColour("Black");
        mapped.setSize("M");
        mapped.setRating(4);

        Product saved = new Product();
        saved.setId(100);
        saved.setProductName("Phone");

        ProductDTO savedDto = ProductDTO.builder().id(100).name("Phone").build();

        when(mapper.toEntity(input)).thenReturn(mapped);
        when(repo.save(mapped)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(savedDto);

        ProductDTO out = service.create(input);

        assertEquals(100, out.getId());
        assertEquals("Phone", out.getName());
        verify(repo).save(mapped);
        verify(mapper).toDto(saved);
    }

    @Test
    @DisplayName("create() rejects missing name or unitPrice")
    void create_missingFields() {
        // missing name
        ProductDTO a = ProductDTO.builder().name(" ").unitPrice(new BigDecimal("1.00")).build();
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> service.create(a));
        assertEquals("Name and unitPrice are required", ex1.getMessage());

        // missing price
        ProductDTO b = ProductDTO.builder().name("X").unitPrice(null).build();
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> service.create(b));
        assertEquals("Name and unitPrice are required", ex2.getMessage());

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("create() rejects negative price")
    void create_negativePrice() {
        ProductDTO a = ProductDTO.builder().name("X").unitPrice(new BigDecimal("-1.00")).build();
        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.create(a));
        assertEquals("unitPrice cannot be negative", ex.getMessage());
        verify(repo, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // update(Integer id, ProductDTO dto)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("update() updates provided fields and maps to DTO")
    void update_success() {
        Product existing = new Product();
        existing.setId(10);
        existing.setProductName("Old");
        existing.setBrand("OldBrand");
        existing.setColour("Red");
        existing.setSize("S");
        existing.setRating(3);
        existing.setUnitPrice(new BigDecimal("10.00"));

        ProductDTO patch = ProductDTO.builder()
                .name("NewName")
                .brand("NewBrand")
                .colour("Blue")
                .size("M")
                .rating(5)
                .unitPrice(new BigDecimal("20.50"))
                .build();

        Product saved = new Product();
        saved.setId(10);
        saved.setProductName("NewName");
        saved.setBrand("NewBrand");
        saved.setColour("Blue");
        saved.setSize("M");
        saved.setRating(5);
        saved.setUnitPrice(new BigDecimal("20.50"));

        ProductDTO outDto = ProductDTO.builder()
                .id(10).name("NewName").brand("NewBrand").colour("Blue").size("M").rating(5).unitPrice(new BigDecimal("20.50"))
                .build();

        when(repo.findById(10)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(outDto);

        ProductDTO out = service.update(10, patch);

        assertEquals(10, out.getId());
        assertEquals("NewName", existing.getProductName());
        assertEquals("NewBrand", existing.getBrand());
        assertEquals("Blue", existing.getColour());
        assertEquals("M", existing.getSize());
        assertEquals(5, existing.getRating());
        assertEquals(new BigDecimal("20.50"), existing.getUnitPrice());
    }

    @Test
    @DisplayName("update() rejects negative unitPrice")
    void update_negativePrice() {
        Product existing = new Product(); existing.setId(11);
        when(repo.findById(11)).thenReturn(Optional.of(existing));

        ProductDTO patch = ProductDTO.builder().unitPrice(new BigDecimal("-0.01")).build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.update(11, patch));
        assertEquals("unitPrice cannot be negative", ex.getMessage());
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("update() throws NotFound when product missing")
    void update_notFound() {
        when(repo.findById(99)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.update(99, ProductDTO.builder().build()));
        assertEquals("Product not found", ex.getMessage());
    }

    // ---------------------------------------------------------------------
    // delete(Integer id)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("delete() deletes when exists")
    void delete_success() {
        when(repo.existsById(5)).thenReturn(true);
        service.delete(5);
        verify(repo).deleteById(5);
    }

    @Test
    @DisplayName("delete() throws NotFound when not exists")
    void delete_notFound() {
        when(repo.existsById(77)).thenReturn(false);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.delete(77));
        assertEquals("Product not found", ex.getMessage());
        verify(repo, never()).deleteById(anyInt());
    }

    // ---------------------------------------------------------------------
    // byBrand(String brand)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("byBrand() maps found products to DTOs")
    void byBrand_found() {
        Product p = new Product(); p.setId(1); p.setBrand("Nike");
        ProductDTO d = ProductDTO.builder().id(1).brand("Nike").build();

        when(repo.findByBrandIgnoreCase("NIKE")).thenReturn(List.of(p));
        when(mapper.toDto(p)).thenReturn(d);

        List<ProductDTO> out = service.byBrand("NIKE");

        assertEquals(1, out.size());
        assertEquals("Nike", out.get(0).getBrand());
    }

    @Test
    @DisplayName("byBrand() throws NotFound when empty")
    void byBrand_empty() {
        when(repo.findByBrandIgnoreCase("BrandX")).thenReturn(List.of());
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.byBrand("BrandX"));
        assertEquals("No products found for brand: BrandX", ex.getMessage());
        verify(mapper, never()).toDto(any());
    }

    // ---------------------------------------------------------------------
    // byColour(String colour)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("byColour() maps found products to DTOs")
    void byColour_found() {
        Product p = new Product(); p.setId(2); p.setColour("Black");
        ProductDTO d = ProductDTO.builder().id(2).colour("Black").build();

        when(repo.findByColourIgnoreCase("Black")).thenReturn(List.of(p));
        when(mapper.toDto(p)).thenReturn(d);

        List<ProductDTO> out = service.byColour("Black");

        assertEquals(1, out.size());
        assertEquals("Black", out.get(0).getColour());
    }

    @Test
    @DisplayName("byColour() throws NotFound when empty")
    void byColour_empty() {
        when(repo.findByColourIgnoreCase("Green")).thenReturn(List.of());
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.byColour("Green"));
        assertEquals("No products found for colour: Green", ex.getMessage());
        verify(mapper, never()).toDto(any());
    }

    // ---------------------------------------------------------------------
    // byPrice(BigDecimal min, BigDecimal max)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("byPrice() returns mapped list for valid range")
    void byPrice_validRange() {
        Product p = new Product(); p.setId(3); p.setUnitPrice(new BigDecimal("50.00"));
        ProductDTO d = ProductDTO.builder().id(3).unitPrice(new BigDecimal("50.00")).build();

        when(repo.findByUnitPriceBetween(new BigDecimal("10.00"), new BigDecimal("100.00")))
                .thenReturn(List.of(p));
        when(mapper.toDto(p)).thenReturn(d);

        List<ProductDTO> out = service.byPrice(new BigDecimal("10.00"), new BigDecimal("100.00"));

        assertEquals(1, out.size());
        assertEquals(new BigDecimal("50.00"), out.get(0).getUnitPrice());
    }

    @Test
    @DisplayName("byPrice() rejects null min or max or min>max")
    void byPrice_invalidRangeParams() {
        // null min
        BadRequestException ex1 = assertThrows(BadRequestException.class,
                () -> service.byPrice(null, new BigDecimal("10")));
        assertEquals("Invalid min/max price", ex1.getMessage());

        // null max
        BadRequestException ex2 = assertThrows(BadRequestException.class,
                () -> service.byPrice(new BigDecimal("1"), null));
        assertEquals("Invalid min/max price", ex2.getMessage());

        // min > max
        BadRequestException ex3 = assertThrows(BadRequestException.class,
                () -> service.byPrice(new BigDecimal("11"), new BigDecimal("10")));
        assertEquals("Invalid min/max price", ex3.getMessage());
    }

    @Test
    @DisplayName("byPrice() rejects negative min even when min<max")
    void byPrice_negativeMin() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.byPrice(new BigDecimal("-1"), new BigDecimal("10")));
        assertEquals("min cannot be negative", ex.getMessage());
    }

    // ---------------------------------------------------------------------
    // byName(String name)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("byName() maps found products to DTOs")
    void byName_found() {
        Product p = new Product(); p.setId(4); p.setProductName("Phone Case");
        ProductDTO d = ProductDTO.builder().id(4).name("Phone Case").build();

        when(repo.searchByName("phone")).thenReturn(List.of(p));
        when(mapper.toDto(p)).thenReturn(d);

        List<ProductDTO> out = service.byName("phone");

        assertEquals(1, out.size());
        assertEquals("Phone Case", out.get(0).getName());
    }

    @Test
    @DisplayName("byName() throws NotFound when empty")
    void byName_empty() {
        when(repo.searchByName("nothing")).thenReturn(List.of());
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.byName("nothing"));
        assertEquals("No products found matching name: nothing", ex.getMessage());
        verify(mapper, never()).toDto(any());
    }
}