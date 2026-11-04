package fu.de190484.entity2service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;

/**
 * TODO: Thay thế <Entity2> thành tên Entity thứ 2 theo đề bài (vd: Reservation, Post, Booking...)
 * - Đổi tên bảng trong @Table(name = "...") theo đề bài (vd: reservations, posts)
 * - Đổi tên/kiểu các cột theo DB specification
 */
@Entity
@Table(name = "entity2_table")
public class Entity2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "client_name", nullable = false, length = 100)
    private String clientName;

    @Column(name = "client_email", nullable = false, length = 100)
    private String clientEmail;

    @Column(name = "client_phone", nullable = false, length = 20)
    private String clientPhone;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // vd: numberOfGuests, numberOfPosts

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Entity2Status status;

    @Column(name = "entity1_id", nullable = false)
    private Long entity1Id; // Foreign key tham chiếu sang Entity1 (vd: room_id, user_id)

    public Entity2() {
    }

    public Entity2(Long id, String clientName, String clientEmail, String clientPhone, Date startDate, Date endDate, Integer quantity, BigDecimal totalAmount, Entity2Status status, Long entity1Id) {
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

    public Entity2Status getStatus() {
        return status;
    }

    public void setStatus(Entity2Status status) {
        this.status = status;
    }

    public Long getEntity1Id() {
        return entity1Id;
    }

    public void setEntity1Id(Long entity1Id) {
        this.entity1Id = entity1Id;
    }
}
