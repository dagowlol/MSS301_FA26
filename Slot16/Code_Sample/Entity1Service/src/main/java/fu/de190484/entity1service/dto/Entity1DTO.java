package fu.de190484.entity1service.dto;

import fu.de190484.entity1service.common.Entity1Constants;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * TODO: Thay thế các thuộc tính và validation annotations theo yêu cầu đề bài
 */
public class Entity1DTO {
    private Long id;

    @NotBlank(message = "code is required")
    @Size(max = 50, message = "code max length is 50")
    private String code;

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name max length is 100")
    private String name;

    @NotBlank(message = "type is required")
    @Pattern(regexp = Entity1Constants.TYPE_REGEX, message = "type is invalid")
    private String type;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0", inclusive = false, message = "price must be > 0")
    private BigDecimal price;

    @NotNull(message = "capacity is required")
    @Min(value = 1, message = "capacity must be >= 1")
    @Max(value = 10, message = "capacity must be <= 10")
    private Integer capacity;

    @NotNull(message = "floorOrLevel is required")
    @Min(value = 1, message = "floorOrLevel must be >= 1")
    private Integer floorOrLevel;

    // Optional on create (defaults to AVAILABLE/ACTIVE); validated only when present
    @Pattern(regexp = Entity1Constants.STATUS_REGEX, message = "status is invalid")
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
