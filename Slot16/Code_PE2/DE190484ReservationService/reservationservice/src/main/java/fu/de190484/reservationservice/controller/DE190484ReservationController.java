package fu.de190484.reservationservice.controller;

import fu.de190484.reservationservice.dto.ApiResponseDTO;
import fu.de190484.reservationservice.dto.ReservationDTO;
import fu.de190484.reservationservice.service.ReservationService;
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
@RequestMapping("/api/reservations")
public class DE190484ReservationController {

    private final ReservationService reservationService;

    public DE190484ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // 1. Create Reservation -> 201/1, 406/2, 404/4, 400/5, 500/0
    @PostMapping
    public ResponseEntity<ApiResponseDTO> createReservation(@Valid @RequestBody ReservationDTO reservationDTO, BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponseDTO.error(2, "Data validation failed (" + firstError(br) + "). No data returned."));
        }
        return map(reservationService.createReservation(reservationDTO), HttpStatus.CREATED);
    }

    // 2. Update Reservation -> 200/1, 406/2, 404/4, 500/0
    @PutMapping("/{reservationId}")
    public ResponseEntity<ApiResponseDTO> updateReservation(@PathVariable Long reservationId,
                                                           @RequestBody ReservationDTO reservationDTO,
                                                           BindingResult br) {
        if (br.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponseDTO.error(2, "Data validation failed (" + firstError(br) + "). No data returned."));
        }
        return map(reservationService.updateReservation(reservationId, reservationDTO), HttpStatus.OK);
    }

    // 3. Get Reservation Detail -> 200/1, 404/4, 500/0
    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponseDTO> getReservationDetail(@PathVariable Long reservationId) {
        return map(reservationService.getReservationById(reservationId), HttpStatus.OK);
    }

    // 4. Cancel Reservation -> 200/1, 404/4, 500/0
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponseDTO> cancelReservation(@PathVariable Long reservationId) {
        return map(reservationService.cancelReservation(reservationId), HttpStatus.OK);
    }

    // 5. Get Reservation List -> 200/1, 406/2, 500/0
    @GetMapping
    public ResponseEntity<ApiResponseDTO> getReservationList(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(required = false) String guestName,
                                                             @RequestParam(required = false) String status) {
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponseDTO.error(2, "Data validation failed (page must be >= 0 and size between 1 and 100). No data returned."));
        }
        Pageable pageable = PageRequest.of(page, size);
        return map(reservationService.getReservationList(guestName, status, pageable), HttpStatus.OK);
    }

    private String firstError(BindingResult br) {
        return br.getAllErrors().isEmpty() ? "Data validation failed" : br.getAllErrors().get(0).getDefaultMessage();
    }

    private ResponseEntity<ApiResponseDTO> map(ApiResponseDTO res, HttpStatus okStatus) {
        HttpStatus httpStatus = switch (res.getStatus()) {
            case 1 -> okStatus;
            case 2 -> HttpStatus.NOT_ACCEPTABLE;          // 406
            case 4 -> HttpStatus.NOT_FOUND;               // 404
            case 5 -> HttpStatus.BAD_REQUEST;             // 400
            default -> HttpStatus.INTERNAL_SERVER_ERROR;  // 500
        };
        return ResponseEntity.status(httpStatus).body(res);
    }
}
