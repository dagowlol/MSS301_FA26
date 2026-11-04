package fu.de190484.entity2service.dto;

import java.math.BigDecimal;

/**
 * DTO đại diện cho Entity 1 (Master) nhận về từ Service 1 qua OpenFeign
 */
public class Entity1DTO {
    private Long id;
    private String code;
    private String name;
    private String type;
    private BigDecimal price;
    private Integer capacity;
    private Integer floorOrLevel;
    private String status;

    public Entity1DTO() {
    }

    public Entity1DTO(Long id, String code, String name, String type, BigDecimal price, Integer capacity, Integer floorOrLevel, String status) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
