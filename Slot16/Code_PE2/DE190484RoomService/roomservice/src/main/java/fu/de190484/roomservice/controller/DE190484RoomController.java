package fu.de190484.roomservice.controller;

import fu.de190484.roomservice.dto.ApiResponseDTO;
import fu.de190484.roomservice.dto.RoomDTO;
import fu.de190484.roomservice.service.RoomService;
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

@RestController
@RequestMapping("/api/rooms")
public class DE190484RoomController {

    private final RoomService roomService;

    public DE190484RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // 1. Create Room -> 201/1, 406/2, 226/3, 500/0
    @PostMapping
    public ResponseEntity<ApiResponseDTO> createRoom(@Valid @RequestBody RoomDTO roomDTO, BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponseDTO.error(2, "Data validation failed (" + firstError(br) + "). No data returned."));
        }
        return map(roomService.createRoom(roomDTO), HttpStatus.CREATED);
    }

    // 2. Update Room -> 200/1, 406/2, 226/3, 404/4, 500/0
    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponseDTO> updateRoom(@PathVariable Long roomId,
                                                     @RequestBody RoomDTO roomDTO,
                                                     BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponseDTO.error(2, "Data validation failed (" + firstError(br) + "). No data returned."));
        }
        return map(roomService.updateRoom(roomId, roomDTO), HttpStatus.OK);
    }

    // 3. Get Room Detail -> 200/1, 404/4, 500/0
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponseDTO> getRoomDetail(@PathVariable Long roomId) {
        return map(roomService.getRoomById(roomId), HttpStatus.OK);
    }

    // 4. Set Room to Maintenance -> 200/1, 404/4, 500/0
    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponseDTO> setMaintenance(@PathVariable Long roomId) {
        return map(roomService.setMaintenance(roomId), HttpStatus.OK);
    }

    // 5. Get Room List -> 200/1, 406/2, 500/0
    @GetMapping
    public ResponseEntity<ApiResponseDTO> getRoomList(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String roomType,
                                                      @RequestParam(required = false) String status) {
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponseDTO.error(2, "Data validation failed (page must be >= 0 and size between 1 and 100). No data returned."));
        }
        Pageable pageable = PageRequest.of(page, size);
        return map(roomService.getRoomList(roomType, status, pageable), HttpStatus.OK);
    }

    private String firstError(BindingResult br) {
        return br.getAllErrors().isEmpty() ? "Data validation failed" : br.getAllErrors().get(0).getDefaultMessage();
    }

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

