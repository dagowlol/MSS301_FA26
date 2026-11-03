package fu.de190484.reservationservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import fu.de190484.reservationservice.client.RoomClient;
import fu.de190484.reservationservice.common.ReservationConstants;
import fu.de190484.reservationservice.dto.ApiResponseDTO;
import fu.de190484.reservationservice.dto.PageDTO;
import fu.de190484.reservationservice.dto.ReservationDTO;
import fu.de190484.reservationservice.dto.ReservationDetailDTO;
import fu.de190484.reservationservice.dto.RoomDTO;
import fu.de190484.reservationservice.entity.Reservation;
import fu.de190484.reservationservice.entity.ReservationStatus;
import fu.de190484.reservationservice.repository.ReservationRepository;
import fu.de190484.reservationservice.service.ReservationService;
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
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomClient roomClient;
    private final ObjectMapper objectMapper;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  RoomClient roomClient,
                                  ObjectMapper objectMapper) {
        this.reservationRepository = reservationRepository;
        this.roomClient = roomClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ApiResponseDTO createReservation(ReservationDTO dto) {
        // Date validation: checkOutDate must be after checkInDate
        if (!dto.getCheckOutDate().after(dto.getCheckInDate())) {
            return ApiResponseDTO.error(2, "Data validation failed (checkOutDate must be after checkInDate). No data returned.");
        }

        // Fetch room from RoomService
        RoomDTO room = fetchRoom(dto.getRoomId());
        if (room == null) {
            return ApiResponseDTO.error(4, "Room ID is not found. No data returned.");
        }

        // Room must have status AVAILABLE
        if (!ReservationConstants.ROOM_AVAILABLE.equalsIgnoreCase(room.getStatus())) {
            return ApiResponseDTO.error(5, "Room is not AVAILABLE for reservation. No data returned.");
        }

        // numberOfGuests must not exceed room capacity
        if (room.getCapacity() != null && dto.getNumberOfGuests() > room.getCapacity()) {
            return ApiResponseDTO.error(2, "Data validation failed (numberOfGuests exceeds room capacity). No data returned.");
        }

        // Compute totalAmount = pricePerNight * number of nights
        BigDecimal totalAmount = computeTotalAmount(room.getPricePerNight(), dto.getCheckInDate(), dto.getCheckOutDate());

        Reservation r = new Reservation();
        r.setGuestName(dto.getGuestName());
        r.setGuestEmail(dto.getGuestEmail());
        r.setGuestPhone(dto.getGuestPhone());
        r.setCheckInDate(dto.getCheckInDate());
        r.setCheckOutDate(dto.getCheckOutDate());
        r.setNumberOfGuests(dto.getNumberOfGuests());
        r.setTotalAmount(totalAmount);
        r.setStatus(ReservationStatus.CONFIRMED); // Default status is CONFIRMED
        r.setRoomId(dto.getRoomId());

        Reservation saved = reservationRepository.save(r);
        return ApiResponseDTO.of(1, "Reservation created successfully.", toDTO(saved));
    }

    @Override
    @Transactional
    public ApiResponseDTO updateReservation(Long reservationId, ReservationDTO dto) {
        Reservation r = reservationRepository.findById(reservationId).orElse(null);
        if (r == null) {
            return ApiResponseDTO.error(4, "Reservation ID is not found. No data returned.");
        }

        Date effCheckIn = dto.getCheckInDate() != null ? dto.getCheckInDate() : r.getCheckInDate();
        Date effCheckOut = dto.getCheckOutDate() != null ? dto.getCheckOutDate() : r.getCheckOutDate();

        if (!effCheckOut.after(effCheckIn)) {
            return ApiResponseDTO.error(2, "Data validation failed (checkOutDate must be after checkInDate). No data returned.");
        }

        Long effRoomId = dto.getRoomId() != null ? dto.getRoomId() : r.getRoomId();
        RoomDTO room = fetchRoom(effRoomId);
        if (room == null) {
            return ApiResponseDTO.error(4, "Room ID is not found. No data returned.");
        }

        int effGuests = dto.getNumberOfGuests() != null ? dto.getNumberOfGuests() : r.getNumberOfGuests();
        if (effGuests < 1 || effGuests > 10 || (room.getCapacity() != null && effGuests > room.getCapacity())) {
            return ApiResponseDTO.error(2, "Data validation failed (invalid numberOfGuests). No data returned.");
        }

        ReservationStatus newStatus = null;
        if (dto.getStatus() != null) {
            try {
                newStatus = ReservationStatus.valueOf(dto.getStatus());
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed (invalid status). No data returned.");
            }
        }

        // Recompute total amount
        BigDecimal totalAmount = computeTotalAmount(room.getPricePerNight(), effCheckIn, effCheckOut);

        // Apply updates
        if (dto.getGuestName() != null) r.setGuestName(dto.getGuestName());
        if (dto.getGuestEmail() != null) r.setGuestEmail(dto.getGuestEmail());
        if (dto.getGuestPhone() != null) r.setGuestPhone(dto.getGuestPhone());
        r.setCheckInDate(effCheckIn);
        r.setCheckOutDate(effCheckOut);
        r.setNumberOfGuests(effGuests);
        r.setRoomId(effRoomId);
        r.setTotalAmount(totalAmount);
        if (newStatus != null) r.setStatus(newStatus);

        Reservation saved = reservationRepository.save(r);
        return ApiResponseDTO.of(1, "Reservation updated successfully.", toDTO(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDTO getReservationById(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId).orElse(null);
        if (r == null) {
            return ApiResponseDTO.error(4, "Reservation is not found. No data returned.");
        }
        return ApiResponseDTO.of(1, "Returns the ReservationDetailDTO object in ApiResponseDTO.", toDetailDTO(r));
    }

    @Override
    @Transactional
    public ApiResponseDTO cancelReservation(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId).orElse(null);
        if (r == null) {
            return ApiResponseDTO.error(4, "Reservation is not found. No data returned.");
        }
        r.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(r);
        return ApiResponseDTO.of(1, "Reservation cancelled successfully. No data returned.", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDTO getReservationList(String guestName, String statusStr, Pageable pageable) {
        ReservationStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = ReservationStatus.valueOf(statusStr);
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed (invalid status). No data returned.");
            }
        }

        Page<Reservation> page = reservationRepository.search(emptyToNull(guestName), status, pageable);
        List<ReservationDetailDTO> content = page.getContent().stream().map(this::toDetailDTO).toList();
        PageDTO pageDTO = new PageDTO(
                page.getSize(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                content
        );
        return ApiResponseDTO.of(1, "Returns the list of ReservationDetailDTO objects in ApiResponseDTO.", pageDTO);
    }

    private RoomDTO fetchRoom(Long roomId) {
        if (roomId == null) return null;
        try {
            ApiResponseDTO res = roomClient.getRoomById(roomId);
            if (res == null || res.getStatus() != 1 || res.getData() == null) {
                return null;
            }
            return objectMapper.convertValue(res.getData(), RoomDTO.class);
        } catch (FeignException.NotFound ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal computeTotalAmount(BigDecimal pricePerNight, Date checkIn, Date checkOut) {
        if (pricePerNight == null) return BigDecimal.ZERO;
        long nights = ChronoUnit.DAYS.between(toLocalDate(checkIn), toLocalDate(checkOut));
        if (nights < 1) nights = 1;
        return pricePerNight.multiply(BigDecimal.valueOf(nights));
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private ReservationDTO toDTO(Reservation r) {
        ReservationDTO dto = new ReservationDTO();
        dto.setReservationId(r.getReservationId());
        dto.setGuestName(r.getGuestName());
        dto.setGuestEmail(r.getGuestEmail());
        dto.setGuestPhone(r.getGuestPhone());
        dto.setCheckInDate(r.getCheckInDate());
        dto.setCheckOutDate(r.getCheckOutDate());
        dto.setNumberOfGuests(r.getNumberOfGuests());
        dto.setTotalAmount(r.getTotalAmount());
        dto.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        dto.setRoomId(r.getRoomId());
        return dto;
    }

    private ReservationDetailDTO toDetailDTO(Reservation r) {
        ReservationDetailDTO dto = new ReservationDetailDTO();
        dto.setReservationId(r.getReservationId());
        dto.setGuestName(r.getGuestName());
        dto.setGuestEmail(r.getGuestEmail());
        dto.setGuestPhone(r.getGuestPhone());
        dto.setCheckInDate(r.getCheckInDate());
        dto.setCheckOutDate(r.getCheckOutDate());
        dto.setNumberOfGuests(r.getNumberOfGuests());
        dto.setTotalAmount(r.getTotalAmount());
        dto.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        dto.setRoom(fetchRoom(r.getRoomId()));
        return dto;
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
