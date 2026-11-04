package fu.de190484.entity2service.client;

import fu.de190484.entity2service.dto.ApiResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign Client gọi sang Service 1
 */
@FeignClient(name = "entity1-service", url = "${entity1.service.url}")
public interface Entity1Client {

    @GetMapping("/api/entity1/{id}")
    ApiResponseDTO getById(@PathVariable("id") Long id);
}
