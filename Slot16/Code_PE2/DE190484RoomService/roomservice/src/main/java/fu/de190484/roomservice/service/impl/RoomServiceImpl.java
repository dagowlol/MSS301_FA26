package fu.de190484.roomservice.service.impl;

import fu.de190484.roomservice.dto.ApiResponseDTO;
import fu.de190484.roomservice.dto.PageDTO;
import fu.de190484.roomservice.dto.RoomDTO;
import fu.de190484.roomservice.entity.Room;
import fu.de190484.roomservice.entity.RoomStatus;
import fu.de190484.roomservice.entity.RoomType;
import fu.de190484.roomservice.repository.RoomRepository;
import fu.de190484.roomservice.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public ApiResponseDTO createRoom(RoomDTO dto) {
        // Status 3 -> duplicated roomNumber (HTTP 226)
        if (roomRepository.existsByRoomNumber(dto.getRoomNumber())) {
            return ApiResponseDTO.error(3, "Room number is duplicated. No data returned.");
        }

        RoomType roomType;
        try {
            roomType = RoomType.valueOf(dto.getRoomType());
        } catch (Exception e) {
            return ApiResponseDTO.error(2, "Data validation failed (invalid roomType). No data returned.");
        }

        RoomStatus status = RoomStatus.AVAILABLE; // Default status is AVAILABLE
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                status = RoomStatus.valueOf(dto.getStatus());
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed (invalid status). No data returned.");
            }
        }

        if (dto.getPricePerNight() == null || dto.getPricePerNight().compareTo(BigDecimal.ZERO) <= 0) {
            return ApiResponseDTO.error(2, "Data validation failed (pricePerNight must be > 0). No data returned.");
        }

        if (dto.getCapacity() == null || dto.getCapacity() < 1 || dto.getCapacity() > 10) {
            return ApiResponseDTO.error(2, "Data validation failed (capacity must be 1-10). No data returned.");
        }

        if (dto.getFloor() == null || dto.getFloor() < 1) {
            return ApiResponseDTO.error(2, "Data validation failed (floor must be >= 1). No data returned.");
        }

        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(roomType);
        room.setPricePerNight(dto.getPricePerNight());
        room.setCapacity(dto.getCapacity());
        room.setFloor(dto.getFloor());
        room.setStatus(status);

        Room saved = roomRepository.save(room);
        return ApiResponseDTO.of(1, "Room created successfully.", toDTO(saved));
    }

    @Override
    @Transactional
    public ApiResponseDTO updateRoom(Long roomId, RoomDTO dto) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponseDTO.error(4, "Room is not found. No data returned.");
        }

        // Validate roomNumber if provided
        if (dto.getRoomNumber() != null) {
            if (dto.getRoomNumber().isBlank() || dto.getRoomNumber().length() > 20) {
                return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
            }
            if (roomRepository.existsByRoomNumberAndRoomIdNot(dto.getRoomNumber(), roomId)) {
                return ApiResponseDTO.error(3, "Room number is duplicated. No data returned.");
            }
        }

        // Validate roomType if provided
        RoomType newType = null;
        if (dto.getRoomType() != null) {
            try {
                newType = RoomType.valueOf(dto.getRoomType());
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
            }
        }

        // Validate pricePerNight if provided
        if (dto.getPricePerNight() != null && dto.getPricePerNight().compareTo(BigDecimal.ZERO) <= 0) {
            return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
        }

        // Validate capacity if provided
        if (dto.getCapacity() != null && (dto.getCapacity() < 1 || dto.getCapacity() > 10)) {
            return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
        }

        // Validate floor if provided
        if (dto.getFloor() != null && dto.getFloor() < 1) {
            return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
        }

        // Validate status if provided
        RoomStatus newStatus = null;
        if (dto.getStatus() != null) {
            try {
                newStatus = RoomStatus.valueOf(dto.getStatus());
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
            }
        }

        // Apply partial update
        if (dto.getRoomNumber() != null) room.setRoomNumber(dto.getRoomNumber());
        if (newType != null) room.setRoomType(newType);
        if (dto.getPricePerNight() != null) room.setPricePerNight(dto.getPricePerNight());
        if (dto.getCapacity() != null) room.setCapacity(dto.getCapacity());
        if (dto.getFloor() != null) room.setFloor(dto.getFloor());
        if (newStatus != null) room.setStatus(newStatus);

        Room saved = roomRepository.save(room);
        return ApiResponseDTO.of(1, "Room updated successfully.", toDTO(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDTO getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponseDTO.error(4, "Room is not found. No data returned.");
        }
        return ApiResponseDTO.of(1, "Returns the RoomDTO object in ApiResponseDTO.", toDTO(room));
    }

    @Override
    @Transactional
    public ApiResponseDTO setMaintenance(Long roomId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ApiResponseDTO.error(4, "Room is not found. No data returned.");
        }
        room.setStatus(RoomStatus.MAINTENANCE);
        roomRepository.save(room);
        return ApiResponseDTO.of(1, "Room set to MAINTENANCE successfully. No data returned.", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponseDTO getRoomList(String roomTypeStr, String statusStr, Pageable pageable) {
        RoomType roomType = null;
        if (roomTypeStr != null && !roomTypeStr.isBlank()) {
            try {
                roomType = RoomType.valueOf(roomTypeStr);
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
            }
        }

        RoomStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = RoomStatus.valueOf(statusStr);
            } catch (Exception e) {
                return ApiResponseDTO.error(2, "Data validation failed. No data returned.");
            }
        }

        Page<Room> page = roomRepository.search(roomType, status, pageable);
        List<RoomDTO> content = page.getContent().stream().map(this::toDTO).toList();
        PageDTO pageDTO = new PageDTO(
                page.getSize(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                content
        );
        return ApiResponseDTO.of(1, "Returns the list of RoomDTO objects in ApiResponseDTO.", pageDTO);
    }

    private RoomDTO toDTO(Room r) {
        RoomDTO dto = new RoomDTO();
        dto.setRoomId(r.getRoomId());
        dto.setRoomNumber(r.getRoomNumber());
        dto.setRoomType(r.getRoomType() != null ? r.getRoomType().name() : null);
        dto.setPricePerNight(r.getPricePerNight());
        dto.setCapacity(r.getCapacity());
        dto.setFloor(r.getFloor());
        dto.setStatus(r.getStatus() != null ? r.getStatus().name() : null);
        return dto;
    }
}

