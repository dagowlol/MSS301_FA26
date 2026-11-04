package fu.de190484.entity1service.controller;

import fu.de190484.entity1service.dto.ApiResponseDTO;
import fu.de190484.entity1service.dto.Entity1DTO;
import fu.de190484.entity1service.service.Entity1Service;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Đổi tên Controller theo cú pháp <StudentID><Entity1>Controller
 * Ví dụ: DE190484RoomController, DE190484UserController
 * - Đổi @RequestMapping("/api/...") theo đề (vd: /api/rooms, /api/users)
 */
@RestController
@RequestMapping("/api/entity1")
public class DE190484Entity1Controller {

    private final Entity1Service service;

    public DE190484Entity1Controller(Entity1Service service) {
        this.service = service;
    }

    // 1. Create -> 201/1, 406/2, 226/3, 500/0
    @PostMapping
    public ResponseEntity<ApiResponseDTO> create(@Valid @RequestBody Entity1DTO dto, BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponseDTO.error(2, "Data validation failed (" + firstError(br) + "). No data returned."));
        }
        return map(service.create(dto), HttpStatus.CREATED);
    }

    // 2. Partial Update -> 200/1, 406/2, 226/3, 404/4, 500/0
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> update(@PathVariable Long id,
                                                 @RequestBody Entity1DTO dto,
                                                 BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponseDTO.error(2, "Data validation failed (" + firstError(br) + "). No data returned."));
        }
        return map(service.update(id, dto), HttpStatus.OK);
    }

    // 3. Get Detail -> 200/1, 404/4, 500/0
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> getDetail(@PathVariable Long id) {
        return map(service.getById(id), HttpStatus.OK);
    }

    // 4. Delete or Status update -> 200/1, 404/4, 500/0
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO> deleteOrDeactivate(@PathVariable Long id) {
        return map(service.deleteOrDeactivate(id), HttpStatus.OK);
    }

    // 5. Get List with pagination -> 200/1, 406/2, 500/0
    @GetMapping
    public ResponseEntity<ApiResponseDTO> getList(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) String type,
                                                  @RequestParam(required = false) String status) {
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponseDTO.error(2, "Data validation failed (page must be >= 0 and size between 1 and 100). No data returned."));
        }
        Pageable pageable = PageRequest.of(page, size);
        return map(service.getList(type, status, pageable), HttpStatus.OK);
    }

    private String firstError(BindingResult br) {
        return br.getAllErrors().isEmpty() ? "Data validation failed" : br.getAllErrors().get(0).getDefaultMessage();
    }

    // Map service status -> HTTP status code
    private ResponseEntity<ApiResponseDTO> map(ApiResponseDTO res, HttpStatus okStatus) {
        HttpStatus httpStatus = switch (res.getStatus()) {
            case 1 -> okStatus;
            case 2 -> HttpStatus.NOT_ACCEPTABLE;          // 406
            case 3 -> HttpStatus.IM_USED;                 // 226
            case 4 -> HttpStatus.NOT_FOUND;               // 404
            case 5 -> HttpStatus.BAD_REQUEST;             // 400
            default -> HttpStatus.INTERNAL_SERVER_ERROR;  // 500
        };
        return ResponseEntity.status(httpStatus).body(res);
    }
}
