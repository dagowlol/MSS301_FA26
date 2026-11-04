package fu.de190484.entity2service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

public class Entity2DetailDTO {
    private Long id;
    private String clientName;
    private String clientEmail;
    private String clientPhone;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date startDate;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date endDate;

    private Integer quantity;
    private BigDecimal totalAmount;
    private String status;

    // Chi tiết Entity1 lấy từ Service 1 qua OpenFeign
    private Entity1DTO entity1;

    public Entity2DetailDTO() {
    }

    public Entity2DetailDTO(Long id, String clientName, String clientEmail, String clientPhone, Date startDate, Date endDate, Integer quantity, BigDecimal totalAmount, String status, Entity1DTO entity1) {
        this.id = id;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.clientPhone = clientPhone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.entity1 = entity1;
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

    public Entity1DTO getEntity1() {
        return entity1;
    }

    public void setEntity1(Entity1DTO entity1) {
        this.entity1 = entity1;
    }
}
