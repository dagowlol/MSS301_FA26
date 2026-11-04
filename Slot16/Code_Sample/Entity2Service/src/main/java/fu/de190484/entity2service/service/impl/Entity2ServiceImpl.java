package fu.de190484.entity2service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import fu.de190484.entity2service.client.Entity1Client;
import fu.de190484.entity2service.common.Entity2Constants;
import fu.de190484.entity2service.dto.ApiResponseDTO;
import fu.de190484.entity2service.dto.Entity1DTO;
import fu.de190484.entity2service.dto.Entity2DTO;
import fu.de190484.entity2service.dto.Entity2DetailDTO;
import fu.de190484.entity2service.dto.PageDTO;
import fu.de190484.entity2service.entity.Entity2;
import fu.de190484.entity2service.entity.Entity2Status;
import fu.de190484.entity2service.repository.Entity2Repository;
import fu.de190484.entity2service.service.Entity2Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
public class Entity2ServiceImpl implements Entity2Service {

    private final Entity2Repository repository;
    private final Entity1Client entity1Client;
    private final ObjectMapper objectMapper;

    public Entity2ServiceImpl(Entity2Repository repository,
                             Entity1Client entity1Client,
                             ObjectMapper objectMapper) {
        this.repository = repository;
        this.entity1Client = entity1Client;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ApiResponseDTO create(Entity2DTO dto) {
        // Validate dates: endDate must be after startDate
        if (!dto.getEndDate().after(dto.getStartDate())) {
            return ApiResponseDTO.error(2, "Data validation failed (endDate must be after startDate). No data returned.");
        }

        // Gọi sang Service 1 kiểm tra Entity1
        Entity1DTO entity1 = fetchEntity1(dto.getEntity1Id());
        if (entity1 == null) {
            return ApiResponseDTO.error(4, "Entity1 ID is not found. No data returned.");
        }

        // Entity1 phải có trạng thái AVAILABLE / ACTIVE
        if (!Entity2Constants.ENTITY1_AVAILABLE.equalsIgnoreCase(entity1.getStatus())) {
            return ApiResponseDTO.error(5, "Entity1 is not AVAILABLE for operation. No data returned.");
        }

        // Validate giới hạn (capacity / max daily posts...)
        if (entity1.getCapacity() != null && dto.getQuantity() > entity1.getCapacity()) {
            return ApiResponseDTO.error(2, "Data validation failed (quantity exceeds capacity limit). No data returned.");
        }

        // Tính tổng tiền: price * số ngày/đơn vị
        BigDecimal totalAmount = computeTotal(entity1.getPrice(), dto.getStartDate(), dto.getEndDate());

        Entity2 entity2 = new Entity2();
        entity2.setClientName(dto.getClientName());
        entity2.setClientEmail(dto.getClientEmail());
        entity2.setClientPhone(dto.getClientPhone());
        entity2.setStartDate(dto.getStartDate());
        entity2.setEndDate(dto.getEndDate());
        entity2.setQuantity(dto.getQuantity());
        entity2.setTotalAmount(totalAmount);
        entity2.setStatus(Entity2Status.CONFIRMED); // Mặc định CONFIRMED
        entity2.setEntity1Id(dto.getEntity1Id());

        Entity2 saved = repository.save(entity2);
        return ApiResponseDTO.of(1, "Created successfully.", toDTO(saved));
    }

    @Override
    @Transactional
    public ApiResponseDTO update(Long id, Entity2DTO dto) {
        Entity2 existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return ApiResponseDTO.error(4, "Entity2 ID is not found. No data returned.");
        }

        Date effStart = dto.getStartDate() != null ? dto.getStartDate() : existing.getStartDate();
        Date effEnd = dto.getEndDate() != null ? dto.getEndDate() : existing.getEndDate();

        if (!effEnd.after(effStart)) {
            return ApiResponseDTO.error(2, "Data validation failed (endDate must be after startDate). No data returned.");
        }

        Long effEntity1Id = dto.getEntity1Id() != null ? dto.getEntity1Id() : existing.getEntity1Id();
        Entity1DTO entity1 = fetchEntity1(effEntity1Id);
        if (entity1 == null) {
            return ApiResponseDTO.error(4, "Entity1 ID is not found. No data returned.");
        }

        int effQty = dto.getQuantity() != null ? dto.getQuantity() : existing.getQuantity();
        if (effQty < 1 || (entity1.getCapacity() != null && effQty > entity1.getCapacity())) {
            return ApiResponseDTO.error(2, "Data validation failed (invalid quantity). No data returned.");
        }

        Entity2Status newStatus = null;
        if (dto.getStatus() != null) {
            try {
                newStatus = Entity2Status.valueOf(dto.getStatus());
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed (invalid status). No data returned.");
            }
        }

        BigDecimal totalAmount = computeTotal(entity1.getPrice(), effStart, effEnd);

        if (dto.getClientName() != null) existing.setClientName(dto.getClientName());
        if (dto.getClientEmail() != null) existing.setClientEmail(dto.getClientEmail());
        if (dto.getClientPhone() != null) existing.setClientPhone(dto.getClientPhone());
        existing.setStartDate(effStart);
        existing.setEndDate(effEnd);
        existing.setQuantity(effQty);
        existing.setEntity1Id(effEntity1Id);
        existing.setTotalAmount(totalAmount);
        if (newStatus != null) existing.setStatus(newStatus);

        Entity2 saved = repository.save(existing);
        return ApiResponseDTO.of(1, "Updated successfully.", toDTO(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDTO getById(Long id) {
        Entity2 entity2 = repository.findById(id).orElse(null);
        if (entity2 == null) {
            return ApiResponseDTO.error(4, "Entity2 is not found. No data returned.");
        }
        return ApiResponseDTO.of(1, "Successful", toDetailDTO(entity2));
    }

    @Override
    @Transactional
    public ApiResponseDTO cancel(Long id) {
        Entity2 entity2 = repository.findById(id).orElse(null);
        if (entity2 == null) {
            return ApiResponseDTO.error(4, "Entity2 is not found. No data returned.");
        }
        entity2.setStatus(Entity2Status.CANCELLED);
        repository.save(entity2);
        return ApiResponseDTO.of(1, "Cancelled successfully. No data returned.", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDTO getList(String clientName, String statusStr, Pageable pageable) {
        Entity2Status status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = Entity2Status.valueOf(statusStr);
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed (invalid status). No data returned.");
            }
        }

        Page<Entity2> page = repository.search(emptyToNull(clientName), status, pageable);
        List<Entity2DetailDTO> content = page.getContent().stream().map(this::toDetailDTO).toList();
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

    private Entity1DTO fetchEntity1(Long entity1Id) {
        if (entity1Id == null) return null;
        try {
            ApiResponseDTO res = entity1Client.getById(entity1Id);
            if (res == null || res.getStatus() != 1 || res.getData() == null) {
                return null;
            }
            return objectMapper.convertValue(res.getData(), Entity1DTO.class);
        } catch (FeignException.NotFound ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal computeTotal(BigDecimal price, Date start, Date end) {
        if (price == null) return BigDecimal.ZERO;
        long days = ChronoUnit.DAYS.between(toLocalDate(start), toLocalDate(end));
        if (days < 1) days = 1;
        return price.multiply(BigDecimal.valueOf(days));
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Entity2DTO toDTO(Entity2 e) {
        Entity2DTO dto = new Entity2DTO();
        dto.setId(e.getId());
        dto.setClientName(e.getClientName());
        dto.setClientEmail(e.getClientEmail());
        dto.setClientPhone(e.getClientPhone());
        dto.setStartDate(e.getStartDate());
        dto.setEndDate(e.getEndDate());
        dto.setQuantity(e.getQuantity());
        dto.setTotalAmount(e.getTotalAmount());
        dto.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        dto.setEntity1Id(e.getEntity1Id());
        return dto;
    }

    private Entity2DetailDTO toDetailDTO(Entity2 e) {
        Entity2DetailDTO dto = new Entity2DetailDTO();
        dto.setId(e.getId());
        dto.setClientName(e.getClientName());
        dto.setClientEmail(e.getClientEmail());
        dto.setClientPhone(e.getClientPhone());
        dto.setStartDate(e.getStartDate());
        dto.setEndDate(e.getEndDate());
        dto.setQuantity(e.getQuantity());
        dto.setTotalAmount(e.getTotalAmount());
        dto.setStatus(e.getStatus() != null ? e.getStatus().name() : null);
        dto.setEntity1(fetchEntity1(e.getEntity1Id()));
        return dto;
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
