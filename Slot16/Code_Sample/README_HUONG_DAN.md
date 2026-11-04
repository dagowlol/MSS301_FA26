# HƯỚNG DẪN SỬ DỤNG TEMPLATE MICROSERVICES CHO KỲ THI PE MSS301

Template này được đúc kết từ toàn bộ cấu trúc và yêu cầu của các đề thi Practical Exam (PE) môn **MSS301** (Microservices Architecture) tại FPT University.

---

## 1. TỔNG QUAN KIẾN TRÚC ĐỀ THI

Mọi đề thi PE MSS301 thường tuân thủ mô hình 3 ứng dụng sau:

```
                            Client / Postman / Swagger
                                       │
                                       ▼
                       ┌───────────────────────────────┐
                       │  ApiGateway (Port 8080)       │
                       │  - Spring Cloud Gateway       │
                       │  - CORS WebFilter             │
                       │  - OpenAPI Swagger Aggregator │
                       └───────────────┬───────────────┘
                                       │
                    ┌──────────────────┴──────────────────┐
                    ▼                                     ▼
        /api/v1/{entity1}/**                  /api/v1/{entity2}/**
┌─────────────────────────────────┐   ┌─────────────────────────────────┐
│ Entity1Service (Port 8081)      │   │ Entity2Service (Port 8082)      │
│ - Master Entity (Room, User...) │◄──┤ - Dependent Entity              │
│ - CRUD, Search, Filter          │Feign (Reservation, Post, Booking...)│
│ - Kiểm tra Unique (Code, Name)  │   │ - Validate Entity1 tồn tại      │
│ - Quản lý Trạng thái (Status)   │   │ - Tính toán dữ liệu / Chi phí   │
└─────────────────────────────────┘   └─────────────────────────────────┘
```

---

## 2. CHUẨN MÃ TRẢ VỀ (STATUS CODE VÀ RESPONSE DTO)

Tất cả các API đều trả về dạng `ApiResponseDTO<T>`:
```json
{
  "status": 1,
  "message": "Success message",
  "data": { ... },
  "timestamp": "2026-11-04T12:00:00Z"
}
```

Bảng mã quy chuẩn xuyên suốt các đề thi:

| HTTP Status | Trường `status` | Ý nghĩa | Ví dụ trong đề |
|---|---|---|---|
| **201 Created** | `1` | Tạo mới bản ghi thành công | Tạo mới Entity1 hoặc Entity2 |
| **200 OK** | `1` | Lấy dữ liệu, cập nhật, bảo trì, hủy thành công | Get by ID, Search phân trang, Update |
| **406 Not Acceptable** | `2` | Dữ liệu đầu vào sai validation / logic nghiệp vụ | Sai format regex mã, sức chứa vượt ngưỡng, ngày kết thúc trước ngày bắt đầu |
| **226 IM Used** | `3` | Dữ liệu duy nhất bị trùng lặp | Mã/Code, Username, Room Number đã tồn tại |
| **404 Not Found** | `4` | Không tìm thấy bản ghi | Entity1 ID hoặc Entity2 ID không tồn tại |
| **400 Bad Request** | `5` | Entity1 không ở trạng thái sẵn sàng | Entity1 đang `INACTIVE`, `OCCUPIED`, `MAINTENANCE`... |
| **500 Internal Error** | `0` | Lỗi máy chủ không xác định | Exception chưa bắt được |

---

## 3. CÁCH 1: SỬ DỤNG SCRIPT TỰ ĐỘNG `replace_template.ps1` (KHUYÊN DÙNG)

Trong thư mục `Code_Sample` đã có sẵn file script PowerShell tự động hóa 100% việc đổi tên biến, package, class, file và thư mục.

### Bước 1: Sao chép thư mục
Hãy copy toàn bộ thư mục `Code_Sample` sang một thư mục mới (ví dụ `Code_PE_Thi`) để giữ nguyên bản mẫu gốc.

### Bước 2: Chạy script PowerShell
Mở PowerShell trong thư mục mới và chạy lệnh:

```powershell
.\replace_template.ps1 -StudentID "DE190484" -Entity1 "Room" -Entity2 "Reservation" -Gateway "Hotel"
```

*Trong đó:*
- `-StudentID`: Mã số sinh viên của bạn (ví dụ: `DE190484`).
- `-Entity1`: Tên Entity thứ nhất (Master), ví dụ: `Room`, `User`, `Doctor`, `Vehicle`...
- `-Entity2`: Tên Entity thứ hai (Dependent), ví dụ: `Reservation`, `Post`, `Appointment`, `Rental`...
- `-Gateway`: Tiền tố tên Gateway trong đề bài (mặc định là `Api`, hoặc `Hotel`, `Media`...).

### Kết quả sau khi chạy:
- Toàn bộ nội dung file `.java`, `.xml`, `.properties` được đổi sang tên Entity và MSSV của bạn.
- Các file Java được đổi tên (ví dụ: `DE190484RoomController.java`, `RoomService.java`...).
- Các package được đổi tên chuẩn mực: `fu.de190484.roomservice...`
- Tên thư mục gốc được đổi thành: `DE190484RoomService`, `DE190484ReservationService`, `DE190484HotelGateway`.

---

## 4. CÁCH 2: THAY THẾ THỦ CÔNG BẰNG INTELLIJ IDEA

Nếu không dùng PowerShell, bạn có thể mở project trên IntelliJ và dùng tính năng **Replace in Files** (`Ctrl + Shift + R`):

1. **Thay thế Entity1 (Bật tính năng Match Case):**
   - Tìm `Entity1` -> Thay bằng: `Room` (hoặc tên đề bài, viết hoa chữ cái đầu)
   - Tìm `entity1` -> Thay bằng: `room` (viết thường)
2. **Thay thế Entity2 (Bật tính năng Match Case):**
   - Tìm `Entity2` -> Thay bằng: `Reservation` (viết hoa chữ cái đầu)
   - Tìm `entity2` -> Thay bằng: `reservation` (viết thường)
3. **Thay thế Gateway:**
   - Tìm `ApiGateway` -> Thay bằng: `HotelGateway`
   - Tìm `apigateway` -> Thay bằng: `hotelgateway`
4. **Thay thế Mã số sinh viên:**
   - Tìm `DE190484` -> Thay bằng MSSV của bạn (viết hoa)
   - Tìm `de190484` -> Thay bằng mssv của bạn (viết thường)
5. **Đổi tên file và thư mục:**
   - Dùng `Shift + F6` trên các class và package chính để IntelliJ tự refactor an toàn.

---

## 5. CHECKLIST CÁC BƯỚC HOÀN THIỆN ĐỀ THI

Sau khi đã chuyển đổi template sang tên Entity theo đề bài:

### Bước 1: Đối chiếu Database và cập nhật Entity
- Mở file `Entity1.java` và `Entity2.java`.
- Kiểm tra các trường trong file `create_xxx_sql.txt` đi kèm đề thi.
- Thêm/bớt các trường dữ liệu, annotation `@Column(name = "...")`, `@Enumerated(EnumType.STRING)`.
- Cập nhật các Enum tương ứng (`Type`, `Status`).

### Bước 2: Cập nhật DTO & Validation Rules
- Mở `Entity1DTO.java` và `Entity2DTO.java`.
- Thêm các annotation validation:
  - `@NotBlank(message = "...")` cho chuỗi không được để trống.
  - `@Pattern(regexp = "...", message = "...")` cho mã theo định dạng (ví dụ `^R\\d{3}$`).
  - `@Min`, `@Max` cho số lượng, sức chứa, số người.
  - `@NotNull` cho Date/Integer/Enum.

### Bước 3: Cập nhật Repository
- Bổ sung hàm kiểm tra trùng lặp khóa duy nhất (nếu đề yêu cầu trả về status `3` / `226 IM Used`):
  ```java
  Optional<Entity1> findFirstByCode(String code);
  ```
- Bổ sung hàm tìm kiếm và phân trang:
  ```java
  Page<Entity1> findByTypeAndStatus(EntityType type, EntityStatus status, Pageable pageable);
  ```

### Bước 4: Kiểm tra và điều chỉnh Service logic
- **Tại Entity1Service:**
  - Logic kiểm tra trùng code.
  - Logic phân trang và search kết hợp keyword/status.
- **Tại Entity2Service:**
  - Gọi Feign sang Entity1:
    - Nếu trả về 404 -> quăng lỗi không tìm thấy (Status 4).
    - Nếu Entity1 không ở trạng thái khả dụng (VD: đang bảo trì, đã thuê, inactive) -> quăng lỗi (Status 5 / 400).
  - Logic tính toán: Ví dụ tính `totalCost = nights * pricePerNight` hoặc `fee`.
  - Logic ngày tháng: Ngày kết thúc phải sau ngày bắt đầu, ngày không được trong quá khứ...

### Bước 5: Cấu hình `application.properties`
Kiểm tra cấu hình database kết nối MS SQL Server:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=<DB_NAME>;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=123456
```

### Bước 6: Chạy kiểm thử
1. Biên dịch và kiểm thử nhanh toàn bộ các service:
   ```bash
   mvn clean test-compile
   ```
2. Chạy cả 3 service:
   - Entity1Service (8081)
   - Entity2Service (8082)
   - ApiGateway (8080)
3. Truy cập Swagger UI qua Gateway để test:
   `http://localhost:8080/swagger-ui.html`

Chúc bạn làm bài thi đạt kết quả tuyệt đối!
