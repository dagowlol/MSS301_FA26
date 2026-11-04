package fu.de190484.entity1service.service;

import fu.de190484.entity1service.dto.ApiResponseDTO;
import fu.de190484.entity1service.dto.Entity1DTO;
import fu.de190484.entity1service.dto.PageDTO;
import fu.de190484.entity1service.entity.Entity1;
import fu.de190484.entity1service.entity.Entity1Status;
import fu.de190484.entity1service.entity.Entity1Type;
import fu.de190484.entity1service.repository.Entity1Repository;
import fu.de190484.entity1service.service.impl.Entity1ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Entity1ServiceImplTest {

    @Mock
    private Entity1Repository repository;

    @InjectMocks
    private Entity1ServiceImpl service;

    private Entity1 sampleEntity;
    private Entity1DTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleEntity = new Entity1(1L, "CODE-01", "Sample Item", Entity1Type.TYPE_A, new BigDecimal("100"), 2, 1, Entity1Status.AVAILABLE);
        sampleDTO = new Entity1DTO(null, "CODE-01", "Sample Item", "TYPE_A", new BigDecimal("100"), 2, 1, null);
    }

    @Test
    void testCreate_Success() {
        when(repository.existsByCode("CODE-01")).thenReturn(false);
        when(repository.save(any(Entity1.class))).thenReturn(sampleEntity);

        ApiResponseDTO response = service.create(sampleDTO);
        assertEquals(1, response.getStatus());
        assertNotNull(response.getData());
    }

    @Test
    void testCreate_Duplicate() {
        when(repository.existsByCode("CODE-01")).thenReturn(true);

        ApiResponseDTO response = service.create(sampleDTO);
        assertEquals(3, response.getStatus());
    }

    @Test
    void testUpdate_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(repository.save(any(Entity1.class))).thenReturn(sampleEntity);

        Entity1DTO updateDTO = new Entity1DTO();
        updateDTO.setName("Updated Name");

        ApiResponseDTO response = service.update(1L, updateDTO);
        assertEquals(1, response.getStatus());
    }

    @Test
    void testUpdate_NotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        ApiResponseDTO response = service.update(999L, sampleDTO);
        assertEquals(4, response.getStatus());
    }

    @Test
    void testGetById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        ApiResponseDTO response = service.getById(1L);
        assertEquals(1, response.getStatus());
    }

    @Test
    void testGetList_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.search(null, null, pageable)).thenReturn(new PageImpl<>(List.of(sampleEntity), pageable, 1));

        ApiResponseDTO response = service.getList(null, null, pageable);
        assertEquals(1, response.getStatus());
        assertTrue(response.getData() instanceof PageDTO);
    }
}
