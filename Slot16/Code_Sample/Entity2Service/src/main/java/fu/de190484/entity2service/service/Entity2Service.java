package fu.de190484.entity2service.service;

import fu.de190484.entity2service.dto.ApiResponseDTO;
import fu.de190484.entity2service.dto.Entity2DTO;
import org.springframework.data.domain.Pageable;

public interface Entity2Service {
    ApiResponseDTO create(Entity2DTO dto);
    ApiResponseDTO update(Long id, Entity2DTO dto);
    ApiResponseDTO getById(Long id);
    ApiResponseDTO cancel(Long id);
    ApiResponseDTO getList(String clientName, String status, Pageable pageable);
}
