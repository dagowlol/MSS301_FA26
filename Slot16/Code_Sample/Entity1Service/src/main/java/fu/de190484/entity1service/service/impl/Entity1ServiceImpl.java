package fu.de190484.entity1service.service.impl;

import fu.de190484.entity1service.dto.ApiResponseDTO;
import fu.de190484.entity1service.dto.Entity1DTO;
import fu.de190484.entity1service.dto.PageDTO;
import fu.de190484.entity1service.entity.Entity1;
import fu.de190484.entity1service.entity.Entity1Status;
import fu.de190484.entity1service.entity.Entity1Type;
import fu.de190484.entity1service.repository.Entity1Repository;
import fu.de190484.entity1service.service.Entity1Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class Entity1ServiceImpl implements Entity1Service {

    private final Entity1Repository repository;

    public Entity1ServiceImpl(Entity1Repository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ApiResponseDTO create(Entity1DTO dto) {
        // Status 3 -> Trùng mã duy nhất (HTTP 226)
        if (repository.existsByCode(dto.getCode())) {
            return ApiResponseDTO.error(3, "Identifier code is duplicated. No data returned.");
        }

        // Validate type
        Entity1Type type;
        try {
            type = Entity1Type.valueOf(dto.getType());
        } catch (Exception e) {
            return ApiResponseDTO.error(2, "Data validation failed (invalid type). No data returned.");
        }

        // Default status
        Entity1Status status = Entity1Status.AVAILABLE;
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                status = Entity1Status.valueOf(dto.getStatus());
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed (invalid status). No data returned.");
            }
        }

        // Validate số liệu
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return ApiResponseDTO.error(2, "Data validation failed (price must be > 0). No data returned.");
        }
        if (dto.getCapacity() == null || dto.getCapacity() < 1 || dto.getCapacity() > 10) {
            return ApiResponseDTO.error(2, "Data validation failed (capacity must be 1-10). No data returned.");
        }
        if (dto.getFloorOrLevel() == null || dto.getFloorOrLevel() < 1) {
            return ApiResponseDTO.error(2, "Data validation failed (floorOrLevel must be >= 1). No data returned.");
        }

        Entity1 entity = new Entity1();
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setType(type);
        entity.setPrice(dto.getPrice());
        entity.setCapacity(dto.getCapacity());
        entity.setFloorOrLevel(dto.getFloorOrLevel());
        entity.setStatus(status);

        Entity1 saved = repository.save(entity);
        return ApiResponseDTO.of(1, "Created successfully.", toDTO(saved));
    }

    @Override
    @Transactional
    public ApiResponseDTO update(Long id, Entity1DTO dto) {
        Entity1 entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return ApiResponseDTO.error(4, "Entity is not found. No data returned.");
        }

        // Partial update: Validate nếu trường được truyền vào
        if (dto.getCode() != null) {
            if (dto.getCode().isBlank() || dto.getCode().length() > 50) {
                return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
            }
            if (repository.existsByCodeAndIdNot(dto.getCode(), id)) {
                return ApiResponseDTO.error(3, "Identifier code is duplicated. No data returned.");
            }
        }

        Entity1Type newType = null;
        if (dto.getType() != null) {
            try {
                newType = Entity1Type.valueOf(dto.getType());
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
            }
        }

        if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
        }
        if (dto.getCapacity() != null && (dto.getCapacity() < 1 || dto.getCapacity() > 10)) {
            return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
        }
        if (dto.getFloorOrLevel() != null && dto.getFloorOrLevel() < 1) {
            return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
        }

        Entity1Status newStatus = null;
        if (dto.getStatus() != null) {
            try {
                newStatus = Entity1Status.valueOf(dto.getStatus());
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
            }
        }

        // Áp dụng cập nhật các trường không null (partial update)
        if (dto.getCode() != null) entity.setCode(dto.getCode());
        if (dto.getName() != null) entity.setName(dto.getName());
        if (newType != null) entity.setType(newType);
        if (dto.getPrice() != null) entity.setPrice(dto.getPrice());
        if (dto.getCapacity() != null) entity.setCapacity(dto.getCapacity());
        if (dto.getFloorOrLevel() != null) entity.setFloorOrLevel(dto.getFloorOrLevel());
        if (newStatus != null) entity.setStatus(newStatus);

        Entity1 saved = repository.save(entity);
        return ApiResponseDTO.of(1, "Updated successfully.", toDTO(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDTO getById(Long id) {
        Entity1 entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return ApiResponseDTO.error(4, "Entity is not found. No data returned.");
        }
        return ApiResponseDTO.of(1, "Successful", toDTO(entity));
    }

    @Override
    @Transactional
    public ApiResponseDTO deleteOrDeactivate(Long id) {
        Entity1 entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return ApiResponseDTO.error(4, "Entity is not found. No data returned.");
        }
        // Chuyển status về MAINTENANCE / DEACTIVATED theo đề
        entity.setStatus(Entity1Status.MAINTENANCE);
        repository.save(entity);
        return ApiResponseDTO.of(1, "Status updated successfully. No data returned.", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDTO getList(String typeStr, String statusStr, Pageable pageable) {
        Entity1Type type = null;
        if (typeStr != null && !typeStr.isBlank()) {
            try {
                type = Entity1Type.valueOf(typeStr);
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
            }
        }

        Entity1Status status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = Entity1Status.valueOf(statusStr);
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
            }
        }

        Page<Entity1> page = repository.search(type, status, pageable);
        List<Entity1DTO> content = page.getContent().stream().map(this::toDTO).toList();
        PageDTO pageDTO = new PageDTO(
                page.getSize(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                content
        );
        return ApiResponseDTO.of(1, "Successful", pageDTO);
    }

    private Entity1DTO toDTO(Entity1 e) {
        Entity1DTO dto = new Entity1DTO();
        dto.setId(e.getId());
        dto.setCode(e.getCode());
        dto.setName(e.getName());
        dto.setType(e.getType() != null ? e.getType().name() : null);
        dto.setPrice(e.getPrice());
        dto.setCapacity(e.getCapacity());
        dto.setFloorOrLevel(e.getFloorOrLevel());
        dto.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        return dto;
    }
}
