package fu.de190484.roomservice.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name="rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true,columnDefinition="NVARCHAR(20)")
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, columnDefinition="NVARCHAR(10)")
    private RoomType role;

    @Column(name = "price_per_night", nullable = false, precision=18, scale=2)
    private BigDecimal pricePerNight;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private RoomStatus status;

    public Room() {
    }

    public Room(Long id, String roomNumber, RoomType role, BigDecimal pricePerNight, Integer capacity, Integer floor, RoomStatus status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.role = role;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.floor = floor;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRole() {
        return role;
    }

    public void setRole(RoomType role) {
        this.role = role;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }
}
