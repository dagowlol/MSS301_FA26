package fu.de190484.entity1service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * TODO: Thay thế <Entity1> thành tên Entity đề bài (vd: Room, User, Doctor, Vehicle...)
 * - Đổi tên bảng trong @Table(name = "...") theo đề bài (vd: rooms, users)
 * - Đổi/thêm/bớt các @Column theo bảng database specification
 */
@Entity
@Table(name = "entity1_table")
public class Entity1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Trường duy nhất (unique code/number/username)
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private Entity1Type type;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "floor_or_level", nullable = false)
    private Integer floorOrLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Entity1Status status;

    public Entity1() {
    }

    public Entity1(Long id, String code, String name, Entity1Type type, BigDecimal price, Integer capacity, Integer floorOrLevel, Entity1Status status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.price = price;
        this.capacity = capacity;
        this.floorOrLevel = floorOrLevel;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Entity1Type getType() {
        return type;
    }

    public void setType(Entity1Type type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getFloorOrLevel() {
        return floorOrLevel;
    }

    public void setFloorOrLevel(Integer floorOrLevel) {
        this.floorOrLevel = floorOrLevel;
    }

    public Entity1Status getStatus() {
        return status;
    }

    public void setStatus(Entity1Status status) {
        this.status = status;
    }
}
