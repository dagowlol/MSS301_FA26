# FUCinema Booking System – Assignment 01

Hệ thống đặt vé xem phim microservices với **API Gateway** xác thực JWT.
Java 21 · Spring Boot 4.1.0 · Spring Cloud 2025.1.3 · SQL Server 2022 · MongoDB 7.0.5 · MySQL 8.3.0 (Docker).

## 1. Kiến trúc

```
Postman ──► api-gateway :9000 ──┬─► customer-service :8081 (SQL Server – cinema_customer)
                                 ├─► movie-service    :8082 (MongoDB  – cinema_movie)
                                 └─► booking-service  :8083 (MySQL     – cinema_booking)
                                             └── OpenFeign ──► movie-service :8082
```

Chỉ **api-gateway** kiểm tra token. Các service phía sau **không** có Spring Security, chỉ đọc các
header `X-User-Id`, `X-User-Email`, `X-User-Role` do gateway chèn vào. Vì vậy **luôn test qua cổng 9000**;
gọi thẳng 8081/8083 các API cần user sẽ trả `401 Missing user context header 'X-User-Id'`.

## 2. Chạy hệ thống

```bash
cd fu-cinema
docker compose up -d          # SQL Server + MongoDB + MySQL, đợi cinema-sqlserver healthy
```

Theo thứ tự (mỗi service một terminal, hoặc dùng cửa sổ *Services* trong IntelliJ):

```
1. customer-service   :8081   mvn spring-boot:run
2. movie-service      :8082   mvn spring-boot:run   (tự seed dữ liệu mẫu lần đầu)
3. booking-service    :8083   mvn spring-boot:run
4. api-gateway        :9000   mvn spring-boot:run
```

Kiểm tra nhanh:

```bash
curl http://localhost:9000/actuator/health     # {"status":"UP"}
curl http://localhost:9000/api/movies
```

## 3. Tài khoản test

| Vai trò | Email | Password | Ghi chú |
|---|---|---|---|
| Admin | `admin@fucinema.com` | `@@abc123@@` | Lưu trong `application.properties`, không có trong DB |
| Customer | `an@gmail.com` | `123456` | ID 1, ACTIVE |
| Customer | `binh@gmail.com` | `123456` | ID 2, ACTIVE |
| Customer | `chi@gmail.com` | `123456` | ID 3, INACTIVE (login → 403) |

Lấy token:

```bash
curl -X POST http://localhost:9000/api/auth/login -H "Content-Type: application/json" \
     -d "{\"email\":\"admin@fucinema.com\",\"password\":\"@@abc123@@\"}"
```

JWT ký HS256 chứa `{sub, uid, role, iat, exp}`; dán vào <https://jwt.io> để xem payload.
`app.jwt.secret` phải **trùng** giữa `customer-service` và `api-gateway`, dài ≥ 32 ký tự.

## 4. Kiểm thử bằng Postman

1. Import `postman/FUCinemaBookingSystem.postman_collection.json` và `postman/FUCinema-Local.postman_environment.json`.
2. Chọn environment `FUCinema-Local`.
3. Chuột phải collection → **Run collection**, giữ thứ tự folder `01-Auth` → `08-Report`, **Run**.

Các request được sắp xếp theo đúng thứ tự chạy, request sau dùng biến do request trước lưu lại
(`adminToken`, `customerToken`, `genreId`, `roomId`, `movieId`, `showtimeId`, `bookingId`…).

| Folder | Phạm vi | Kết quả mong đợi |
|---|---|---|
| `01-Auth` | F1 | Login Admin/Customer, sai mật khẩu 401, INACTIVE 403, thiếu/sai token 401 |
| `02-Customer` | F2, F3 | Đăng ký, trùng email 409, validation 400, profile, đổi mật khẩu, Admin CRUD + soft delete |
| `03-Genre-Room` | F4 | Genre/Room CRUD, guard khi còn phim/suất chiếu, phân quyền |
| `04-Movie` | F5 | Tìm kiếm/lọc phim, `genreId` không tồn tại → 404 |
| `05-Showtime` | F6 | `endTime` tự tính, chồng lịch 409, hủy suất chiếu (soft delete) |
| `06-Booking` | F7 | Seat map, đặt vé, ghế trùng 409, ghế ngoài sơ đồ 400, tối đa 8 vé |
| `07-History-Cancel` | F8 | Lịch sử, quyền sở hữu 403, hủy vé và giải phóng ghế |
| `08-Report` | F9 | Doanh thu theo phim, sort giảm dần, bỏ qua booking đã hủy |

Kiểm tra thêm BR14 (503 khi movie-service sập) làm tay: dừng movie-service, gửi lại request 6.13 với
ghế `B2` → kỳ vọng `503 Movie service is unavailable...`, rồi bật lại movie-service.

## 5. Quy tắc nghiệp vụ chính (BR)

| Mã | Nội dung | Kiểm tra |
|---|---|---|
| BR02 | Chặn tài khoản `INACTIVE` khi login | Postman 1.4 |
| BR03 | Không xóc genre/room/movie đang được tham chiếu | Postman 3.6, 4.8, 5.11, 5.12 |
| BR04 – BR06 | Suất chiếu: `SCHEDULED`/`CANCELLED`, không chồng lịch, `endTime = startTime + duration` | Postman 5.1 – 5.10 |
| BR07 | Tối đa 8 vé, không trùng ghế trong cùng request | Postman 6.6, 6.9, 6.10 |
| BR08 | Ghế phải nằm trong sơ đồ phòng | Postman 6.4, 6.5 |
| BR09 | Không bán lại ghế đã có booking `CONFIRMED` | Postman 6.3 |
| BR10 | Giá và tổng tiền do server tính, snapshot thông tin phim | Postman 6.2 |
| BR11 | Customer chỉ xem/hủy booking của chính mình | Postman 7.2, 7.4 |
| BR12 | Hủy booking trước giờ chiếu ≥ 2 giờ | Postman 7.5, 7.6 |
| BR13 | Report: `startDate <= endDate` | Postman 8.2 |
| BR14 | movie-service không phản hồi → 503 | Postman 6.15 (thủ công) |
| BR15 | MongoDB không có khóa ngoại → phải tự kiểm tra ID tham chiếu | Postman 4.7, 5.13 |
| BR16 | Tiếng Việt có dấu lưu đúng trên SQL Server (`NVARCHAR`) | Postman 2.6 |

## 6. Cấu trúc thư mục

```
fu-cinema/
├── docker-compose.yml            # sqlserver + sqlserver-init + mongo + mysql
├── sqlserver/init.sql            # tạo database cinema_customer
├── mysql/init.sql                # tạo database cinema_booking
├── customer-service/             # F1–F3, port 8081, SQL Server + Flyway
├── movie-service/                # F4–F6, port 8082, MongoDB + DataSeeder
├── booking-service/              # F7–F9, port 8083, MySQL + Flyway + OpenFeign
├── api-gateway/                  # F10, port 9000, JWT + role-based routing
└── postman/                      # F11, collection + environment
```

## 7. Xử lý lỗi thường gặp

| Hiện tượng | Cách xử lý |
|---|---|
| `Unknown database 'cinema_booking'` | `docker compose down`, xóa `docker/mysql/data`, `docker compose up -d` |
| `Port 3306/27017 is already allocated` | `docker stop mysql mongo` (container của Part 1) |
| `FlywayValidateException: checksum mismatch` | Không sửa `V1`/`V2` sau khi đã chạy – tạo `V3__...` |
| Mọi request có token đều 401 | `app.jwt.secret` lệch giữa gateway và customer-service, hoặc token hết hạn (60') |
| Đúng role mà vẫn 403 | Thứ tự `requestMatchers` trong `SecurityConfig`: quy tắc cụ thể phải đặt trước quy tắc tổng quát |
| `401 Missing user context header 'X-User-Id'` | Đang gọi thẳng 8081/8083 – hãy gọi qua `{{gateway}}` |
| Đặt vé trả 503 | Bật movie-service, kiểm tra `movie.service.url` |
| Report luôn rỗng | Kiểm tra biến `today` ở Postman có khớp ngày server không |
| `ticketPrice` trong MongoDB là chuỗi | Thiếu `@Field(targetType = FieldType.DECIMAL128)` – xóa dữ liệu cũ rồi seed lại |

## 8. Quy ước commit

`<type>(<scope>): <subject>` + footer `Refs: TODO <x.y>`; **1 TODO = 1 commit**.
Xem `Assignment1_Guide.md` mục 9 để biết đầy đủ danh sách commit message cho từng TODO.
