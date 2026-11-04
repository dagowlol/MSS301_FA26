package fu.de190484.entity2service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fu.de190484.entity2service.client.Entity1Client;
import fu.de190484.entity2service.dto.ApiResponseDTO;
import fu.de190484.entity2service.dto.Entity1DTO;
import fu.de190484.entity2service.dto.Entity2DTO;
import fu.de190484.entity2service.dto.Entity2DetailDTO;
import fu.de190484.entity2service.dto.PageDTO;
import fu.de190484.entity2service.entity.Entity2;
import fu.de190484.entity2service.entity.Entity2Status;
import fu.de190484.entity2service.repository.Entity2Repository;
import fu.de190484.entity2service.service.impl.Entity2ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Entity2ServiceImplTest {

    @Mock
    private Entity2Repository repository;

    @Mock
    private Entity1Client entity1Client;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private Entity2ServiceImpl service;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private Entity2 sampleEntity2;
    private Entity2DTO sampleDTO;
    private Entity1DTO sampleEntity1;

    @BeforeEach
    void setUp() throws Exception {
        Date start = sdf.parse("01/12/2026");
        Date end = sdf.parse("03/12/2026");

        sampleEntity1 = new Entity1DTO(1L, "CODE-01", "Item 1", "TYPE_A", new BigDecimal("100"), 5, 1, "AVAILABLE");

        sampleEntity2 = new Entity2(
                1L, "Customer A", "cust@example.com", "0123456789",
                start, end, 2, new BigDecimal("200"),
                Entity2Status.CONFIRMED, 1L
        );

        sampleDTO = new Entity2DTO(
                null, "Customer A", "cust@example.com", "0123456789",
                start, end, 2, null, null, 1L
        );
    }

    @Test
    void testCreate_Success() {
        when(entity1Client.getById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleEntity1));
        when(repository.save(any(Entity2.class))).thenReturn(sampleEntity2);

        ApiResponseDTO response = service.create(sampleDTO);
        assertEquals(1, response.getStatus());
        assertNotNull(response.getData());
    }

    @Test
    void testCreate_InvalidDates() throws Exception {
        sampleDTO.setEndDate(sdf.parse("01/12/2026")); // Same day
        ApiResponseDTO response = service.create(sampleDTO);
        assertEquals(2, response.getStatus());
    }

    @Test
    void testCreate_Entity1NotFound() {
        when(entity1Client.getById(1L)).thenReturn(null);

        ApiResponseDTO response = service.create(sampleDTO);
        assertEquals(4, response.getStatus());
    }

    @Test
    void testCreate_Entity1NotAvailable() {
        sampleEntity1.setStatus("OCCUPIED");
        when(entity1Client.getById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleEntity1));

        ApiResponseDTO response = service.create(sampleDTO);
        assertEquals(5, response.getStatus());
    }

    @Test
    void testCreate_CapacityExceeded() {
        sampleDTO.setQuantity(10); // Exceeds capacity 5
        when(entity1Client.getById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleEntity1));

        ApiResponseDTO response = service.create(sampleDTO);
        assertEquals(2, response.getStatus());
    }

    @Test
    void testGetById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity2));
        when(entity1Client.getById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleEntity1));

        ApiResponseDTO response = service.getById(1L);
        assertEquals(1, response.getStatus());
        assertTrue(response.getData() instanceof Entity2DetailDTO);
    }

    @Test
    void testCancel_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity2));
        when(repository.save(any(Entity2.class))).thenReturn(sampleEntity2);

        ApiResponseDTO response = service.cancel(1L);
        assertEquals(1, response.getStatus());
        assertEquals(Entity2Status.CANCELLED, sampleEntity2.getStatus());
    }

    @Test
    void testGetList_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.search(null, null, pageable)).thenReturn(new PageImpl<>(List.of(sampleEntity2), pageable, 1));
        when(entity1Client.getById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleEntity1));

        ApiResponseDTO response = service.getList(null, null, pageable);
        assertEquals(1, response.getStatus());
        assertTrue(response.getData() instanceof PageDTO);
    }
}
