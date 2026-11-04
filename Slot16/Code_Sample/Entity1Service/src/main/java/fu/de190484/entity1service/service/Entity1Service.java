package fu.de190484.entity1service.service;

import fu.de190484.entity1service.dto.ApiResponseDTO;
import fu.de190484.entity1service.dto.Entity1DTO;
import org.springframework.data.domain.Pageable;

public interface Entity1Service {
    ApiResponseDTO create(Entity1DTO dto);
    ApiResponseDTO update(Long id, Entity1DTO dto);
    ApiResponseDTO getById(Long id);
    ApiResponseDTO deleteOrDeactivate(Long id);
    ApiResponseDTO getList(String type, String status, Pageable pageable);
}
