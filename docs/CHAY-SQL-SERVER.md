# Chạy website với SQL Server và tài khoản Windows

Máy chủ của bạn: **SPQR**, cổng **1433**, cơ sở dữ liệu **cake_shop**, đăng nhập **Windows Authentication**. Ứng dụng sử dụng tài khoản Windows đang chạy Eclipse; không cần nhập mật khẩu SQL Server.

## Chạy trong Eclipse

1. Chọn dự án **WebTMDT**, nhấn **F5** để cập nhật các file vừa sửa.
2. Nhấp phải dự án → **Maven → Update Project** → OK.
3. Nhấp phải dự án → **Run As → Maven build…**:
   - **Base directory:** thư mục dự án chứa `pom.xml`.
   - **Goals:** `initialize`.
   - **Profiles:** để trống, không nhập `pom.xml`.
   - Nhấn **Run**, đợi `BUILD SUCCESS`. Maven tải thư viện xác thực Microsoft vào `native/`.
4. Trong dự án, nhấp phải **Chay-website-SQL-Server.launch** → **Run As → Chay-website-SQL-Server**. Nếu chưa xuất hiện, mở **Run → Run Configurations → Java Application → Chay-website-SQL-Server** rồi nhấn **Run**.
5. Đợi dòng `Started WebTmdtApplication` và thông báo Tomcat chạy cổng `8080`.
6. Mở **http://localhost:8080**.

File cấu hình chạy đã thêm đường dẫn thư viện xác thực Windows. Dùng JDK 17/21/23 **64-bit**. Không cần sửa PATH của Windows hay chép DLL vào thư mục hệ thống.

Nếu bạn đã đặt biến `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` hoặc `SPRING_PROFILES_ACTIVE` ở cấu hình chạy Eclipse, kiểm tra và xóa giá trị cũ dành cho MySQL. Không bật cấu hình `test` khi chạy website. Có thể giữ các biến `ADMIN_EMAIL`, `ADMIN_PASSWORD` để tạo quản trị viên như trước.

## Cách khác: chạy qua Maven

Trong **Run As → Maven build…**, đặt:

- **Goals:** `spring-boot:run`.
- **Profiles:** để trống.

Hoặc mở PowerShell tại thư mục dự án:

```powershell
.\mvnw.cmd spring-boot:run
```

Maven tự chuẩn bị thư viện Windows và đường dẫn DLL. Không dùng `-P pom.xml`: `pom.xml` là tên file cấu hình dự án, không phải tên cấu hình chạy.

## Phân biệt chạy kiểm thử và chạy website

| Lệnh | Ý nghĩa |
|---|---|
| `initialize` | Chuẩn bị thư viện xác thực Windows |
| `test` | Chạy kiểm thử trên cơ sở dữ liệu H2 tạm thời |
| `verify` | Kiểm thử và đóng gói; không giữ website hoạt động |
| `spring-boot:run` | Khởi động website với SQL Server thật |

Nếu log có `CakeShopIntegrationTests`, `profile: test` hoặc `jdbc:h2:mem:cakes`, bạn đang chạy kiểm thử. Các dòng cảnh báo Mockito/Java agent không phải lỗi kết nối SQL Server; xem kết quả cuối `Tests run` và `BUILD SUCCESS` hoặc `BUILD FAILURE`.

## Cấu hình kết nối hiện tại

```properties
spring.datasource.url=${DB_URL:jdbc:sqlserver://SPQR:1433;databaseName=cake_shop;integratedSecurity=true;authenticationScheme=NativeAuthentication;encrypt=true;trustServerCertificate=true;loginTimeout=10}
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.username=${DB_USERNAME:}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=validate
```

`trustServerCertificate=true` phục vụ SQL Server cục bộ dùng chứng chỉ tự ký; kết nối vẫn được mã hóa nhưng bỏ kiểm tra chứng chỉ. Khi triển khai máy chủ thật, cấu hình chứng chỉ tin cậy và đổi thành `false`.

Ứng dụng chỉ kiểm tra cấu trúc bảng bằng `validate`, không tự tạo/xóa bảng. Các trường chữ tiếng Việt đã ánh xạ sang `NVARCHAR` bằng `@Nationalized`. Nếu database đã tạo thành công thì không chạy lại file SQL tạo bảng.

## Nếu còn lỗi

- **profile "pom.xml" could not be activated:** xóa `pom.xml` khỏi ô Profiles.
- **not configured for integrated authentication:** chạy Maven `initialize`, kiểm tra file `native/mssql-jdbc_auth-12.10.2.x64.dll`, rồi dùng file `.launch` đã cung cấp hoặc Maven `spring-boot:run`.
- **TCP/IP connection failed:** kiểm tra dịch vụ SQL Server, TCP/IP và cổng 1433. Đây là lỗi kết nối, không chữa bằng cách thêm Hibernate dialect.
- **Login failed:** tài khoản Windows đang mở Eclipse phải có quyền truy cập `cake_shop` trên SPQR.
- **Port 8080 already in use:** dừng lần chạy website trước trong Eclipse rồi chạy lại.

Nguồn: [Microsoft hướng dẫn Windows Authentication và java.library.path](https://learn.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url), [Hibernate hướng dẫn kiểu dữ liệu Unicode](https://docs.hibernate.org/orm/6.6/introduction/html_single/).
