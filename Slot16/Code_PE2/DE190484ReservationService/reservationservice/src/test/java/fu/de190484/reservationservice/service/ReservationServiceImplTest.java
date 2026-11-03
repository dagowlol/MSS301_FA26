package fu.de190484.reservationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fu.de190484.reservationservice.client.RoomClient;
import fu.de190484.reservationservice.dto.ApiResponseDTO;
import fu.de190484.reservationservice.dto.PageDTO;
import fu.de190484.reservationservice.dto.ReservationDTO;
import fu.de190484.reservationservice.dto.ReservationDetailDTO;
import fu.de190484.reservationservice.dto.RoomDTO;
import fu.de190484.reservationservice.entity.Reservation;
import fu.de190484.reservationservice.entity.ReservationStatus;
import fu.de190484.reservationservice.repository.ReservationRepository;
import fu.de190484.reservationservice.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomClient roomClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private Reservation sampleReservation;
    private ReservationDTO sampleDTO;
    private RoomDTO sampleRoom;

    @BeforeEach
    void setUp() throws Exception {
        Date checkIn = sdf.parse("01/12/2026");
        Date checkOut = sdf.parse("03/12/2026");

        sampleRoom = new RoomDTO(1L, "A-101", "DOUBLE", new BigDecimal("850000"), 2, 1, "AVAILABLE");

        sampleReservation = new Reservation(
                1L, "John Doe", "john@example.com", "0123456789",
                checkIn, checkOut, 2, new BigDecimal("1700000"),
                ReservationStatus.CONFIRMED, 1L
        );

        sampleDTO = new ReservationDTO(
                null, "John Doe", "john@example.com", "0123456789",
                checkIn, checkOut, 2, null, null, 1L
        );
    }

    @Test
    void testCreateReservation_Success() {
        when(roomClient.getRoomById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleRoom));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(sampleReservation);

        ApiResponseDTO response = reservationService.createReservation(sampleDTO);
        assertEquals(1, response.getStatus());
        assertNotNull(response.getData());
        ReservationDTO created = (ReservationDTO) response.getData();
        assertEquals("CONFIRMED", created.getStatus());
    }

    @Test
    void testCreateReservation_InvalidDates() throws Exception {
        sampleDTO.setCheckOutDate(sdf.parse("01/12/2026")); // Same day as checkInDate
        ApiResponseDTO response = reservationService.createReservation(sampleDTO);
        assertEquals(2, response.getStatus());
    }

    @Test
    void testCreateReservation_RoomNotFound() {
        when(roomClient.getRoomById(1L)).thenReturn(null);

        ApiResponseDTO response = reservationService.createReservation(sampleDTO);
        assertEquals(4, response.getStatus());
    }

    @Test
    void testCreateReservation_RoomNotAvailable() {
        sampleRoom.setStatus("MAINTENANCE");
        when(roomClient.getRoomById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleRoom));

        ApiResponseDTO response = reservationService.createReservation(sampleDTO);
        assertEquals(5, response.getStatus());
    }

    @Test
    void testCreateReservation_CapacityExceeded() {
        sampleDTO.setNumberOfGuests(3); // Capacity is 2
        when(roomClient.getRoomById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleRoom));

        ApiResponseDTO response = reservationService.createReservation(sampleDTO);
        assertEquals(2, response.getStatus());
    }

    @Test
    void testUpdateReservation_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));
        when(roomClient.getRoomById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleRoom));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(sampleReservation);

        ReservationDTO updateDTO = new ReservationDTO();
        updateDTO.setGuestName("Jane Doe");

        ApiResponseDTO response = reservationService.updateReservation(1L, updateDTO);
        assertEquals(1, response.getStatus());
    }

    @Test
    void testUpdateReservation_NotFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        ApiResponseDTO response = reservationService.updateReservation(999L, sampleDTO);
        assertEquals(4, response.getStatus());
    }

    @Test
    void testGetReservationById_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));
        when(roomClient.getRoomById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleRoom));

        ApiResponseDTO response = reservationService.getReservationById(1L);
        assertEquals(1, response.getStatus());
        assertNotNull(response.getData());
        ReservationDetailDTO detail = (ReservationDetailDTO) response.getData();
        assertNotNull(detail.getRoom());
        assertEquals("A-101", detail.getRoom().getRoomNumber());
    }

    @Test
    void testCancelReservation_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(sampleReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(sampleReservation);

        ApiResponseDTO response = reservationService.cancelReservation(1L);
        assertEquals(1, response.getStatus());
        assertEquals(ReservationStatus.CANCELLED, sampleReservation.getStatus());
    }

    @Test
    void testGetReservationList_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(reservationRepository.search(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(sampleReservation), pageable, 1));
        when(roomClient.getRoomById(1L)).thenReturn(ApiResponseDTO.of(1, "Successful", sampleRoom));

        ApiResponseDTO response = reservationService.getReservationList(null, null, pageable);
        assertEquals(1, response.getStatus());
        assertTrue(response.getData() instanceof PageDTO);
        PageDTO pageDTO = (PageDTO) response.getData();
        assertEquals(1, pageDTO.getTotalElements());
    }
}
