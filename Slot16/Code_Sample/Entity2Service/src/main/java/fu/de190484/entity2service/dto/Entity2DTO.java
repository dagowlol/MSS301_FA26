package fu.de190484.entity2service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.de190484.entity2service.common.Entity2Constants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

public class Entity2DTO {
    private Long id;

    @NotBlank(message = "clientName is required")
    @Size(max = 100, message = "clientName max length is 100")
    private String clientName;

    @NotBlank(message = "clientEmail is required")
    @Email(message = "clientEmail is invalid")
    @Size(max = 100, message = "clientEmail max length is 100")
    private String clientEmail;

    @NotBlank(message = "clientPhone is required")
    @Size(max = 20, message = "clientPhone max length is 20")
    private String clientPhone;

    @NotNull(message = "startDate is required")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date startDate;

    @NotNull(message = "endDate is required")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date endDate;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be >= 1")
    private Integer quantity;

    // Server tính toán tự động
    private BigDecimal totalAmount;

    // Optional khi create (mặc định CONFIRMED)
    @Pattern(regexp = Entity2Constants.STATUS_REGEX, message = "status is invalid")
    private String status;

    @NotNull(message = "entity1Id is required")
    private Long entity1Id;

    public Entity2DTO() {
    }

    public Entity2DTO(Long id, String clientName, String clientEmail, String clientPhone, Date startDate, Date endDate, Integer quantity, BigDecimal totalAmount, String status, Long entity1Id) {
        this.id = id;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.clientPhone = clientPhone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.entity1Id = entity1Id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getEntity1Id() {
        return entity1Id;
    }

    public void setEntity1Id(Long entity1Id) {
        this.entity1Id = entity1Id;
    }
}
