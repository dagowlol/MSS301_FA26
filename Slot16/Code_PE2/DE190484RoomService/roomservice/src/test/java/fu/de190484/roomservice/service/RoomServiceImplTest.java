package fu.de190484.roomservice.service;

import fu.de190484.roomservice.dto.ApiResponseDTO;
import fu.de190484.roomservice.dto.PageDTO;
import fu.de190484.roomservice.dto.RoomDTO;
import fu.de190484.roomservice.entity.Room;
import fu.de190484.roomservice.entity.RoomStatus;
import fu.de190484.roomservice.entity.RoomType;
import fu.de190484.roomservice.repository.RoomRepository;
import fu.de190484.roomservice.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room sampleRoom;
    private RoomDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleRoom = new Room(1L, "A-101", RoomType.DOUBLE, new BigDecimal("850000"), 2, 1, RoomStatus.AVAILABLE);
        sampleDTO = new RoomDTO(null, "A-101", "DOUBLE", new BigDecimal("850000"), 2, 1, null);
    }

    @Test
    void testCreateRoom_Success() {
        when(roomRepository.existsByRoomNumber("A-101")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(sampleRoom);

        ApiResponseDTO response = roomService.createRoom(sampleDTO);
        assertEquals(1, response.getStatus());
        assertNotNull(response.getData());
        RoomDTO created = (RoomDTO) response.getData();
        assertEquals("A-101", created.getRoomNumber());
        assertEquals("AVAILABLE", created.getStatus());
    }

    @Test
    void testCreateRoom_DuplicateRoomNumber() {
        when(roomRepository.existsByRoomNumber("A-101")).thenReturn(true);

        ApiResponseDTO response = roomService.createRoom(sampleDTO);
        assertEquals(3, response.getStatus());
        assertNull(response.getData());
    }

    @Test
    void testCreateRoom_InvalidRoomType() {
        sampleDTO.setRoomType("INVALID_TYPE");
        ApiResponseDTO response = roomService.createRoom(sampleDTO);
        assertEquals(2, response.getStatus());
    }

    @Test
    void testUpdateRoom_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(sampleRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(sampleRoom);

        RoomDTO updateDTO = new RoomDTO();
        updateDTO.setPricePerNight(new BigDecimal("900000"));

        ApiResponseDTO response = roomService.updateRoom(1L, updateDTO);
        assertEquals(1, response.getStatus());
    }

    @Test
    void testUpdateRoom_NotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        ApiResponseDTO response = roomService.updateRoom(999L, sampleDTO);
        assertEquals(4, response.getStatus());
    }

    @Test
    void testGetRoomById_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(sampleRoom));

        ApiResponseDTO response = roomService.getRoomById(1L);
        assertEquals(1, response.getStatus());
        assertNotNull(response.getData());
    }

    @Test
    void testGetRoomById_NotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        ApiResponseDTO response = roomService.getRoomById(999L);
        assertEquals(4, response.getStatus());
    }

    @Test
    void testSetMaintenance_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(sampleRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(sampleRoom);

        ApiResponseDTO response = roomService.setMaintenance(1L);
        assertEquals(1, response.getStatus());
        assertEquals(RoomStatus.MAINTENANCE, sampleRoom.getStatus());
    }

    @Test
    void testGetRoomList_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(roomRepository.search(null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(sampleRoom), pageable, 1));

        ApiResponseDTO response = roomService.getRoomList(null, null, pageable);
        assertEquals(1, response.getStatus());
        assertTrue(response.getData() instanceof PageDTO);
        PageDTO pageDTO = (PageDTO) response.getData();
        assertEquals(1, pageDTO.getTotalElements());
    }
}

