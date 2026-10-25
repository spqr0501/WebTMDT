# Kiểm thử và nghiệm thu

## Cập nhật SQL Server ngày 25/10/2026

Đã chuyển trình điều khiển sang Microsoft JDBC, ánh xạ chữ tiếng Việt bằng `@Nationalized` và cấu hình Windows Authentication. Kết nối trực tiếp tới `SPQR:1433`, database `cake_shop` thành công; máy chủ trả phiên bản SQL Server 16.0. Ứng dụng khởi động với `ddl-auto=validate` không có lỗi cấu trúc bảng. Các trang `/`, `/products`, tìm theo từ khóa tiếng Việt `dâu` và `/login` trả HTTP 200 trên cổng kiểm tra 8081.

Lần kiểm tra này chỉ đọc dữ liệu hiện có, không tạo đơn, tài khoản hay sửa tồn kho trên SQL Server thật. Các kiểm thử nghiệp vụ vẫn chạy trên H2 chế độ MSSQLServer. Chưa nghiệm thu trực quan bằng trình duyệt; các ghi chú chưa kiểm tra MySQL bên dưới thuộc phiên bản cũ.

## Kiểm thử tự động

Kết quả ngày 24/10/2026: `mvnw.cmd -q verify` hoàn tất với exit ma 0; **17 kiểm thử, 0 thất bại, 0 lỗi, 0 bỏ qua** (16 integration test + 1 context test). Đã tạo `target/WebTMDT-1.0.0.jar`.

Chạy `mvnw.cmd test` hoặc `mvnw.cmd verify`. Bộ test dùng JUnit 5, Spring Boot Test, MockMvc, Spring Security Test và H2 MySQL mode. Không có kết nối đến cơ sở dữ liệu thật của người dùng.

| Nhóm | Điều kiện được kiểm tra |
|---|---|
| Khởi động | Application context và 8 Repository hoạt động |
| Thymeleaf | Render trang chủ, catalog, chi tiết, giỏ, đăng nhập/đăng ký, địa chỉ, thanh toán, đơn; toàn bộ trang quản trị |
| Tìm kiếm | Tên + danh mục + khoảng giá; từ chối min > max |
| Security | Anonymous không vào quản trị viên; USER bị 403; POST thiếu CSRF bị 403; chặn session của tài khoản bị khóa |
| Đăng ký/login | BCrypt hoạt động; đăng nhập với mật khẩu thật; không chấp nhận vai_tro ADMIN từ form; email trùng không phân biệt hoa thường |
| Quyền sở hữu | Không xem/hủy đơn người khác; không dùng địa chỉ người khác để thanh toán |
| Thanh toán | Tính giá từ server, trần mã giảm giá, miễn ship, bản chụp dữ liệu giá; cùng token chỉ tạo một đơn |
| Hủy | Hoàn kho một lần; không hoàn lượt mã giảm giá; hủy hai lần thất bại |
| Trạng thái | Không nhảy bước; không hủy sau chuẩn bị; bảng tổng quan chỉ tính doanh thu hoàn thành |
| Mã giảm giá lỗi | Mã sai, chưa đủ min, chưa bắt đầu, hết hạn, tắt, hết lượt; thất bại hoàn tác giao dịch kho và đơn |
| Mã giảm giá cố định | Không giảm vượt tổng tiền bánh |
| Kho | Không đặt vượt kho hoặc số lượng âm; không điều chỉnh kho xuống âm |
| Đồng thời | Hai khách mua bánh cuối chỉ một thành công; tranh lượt mã giảm giá cuối chỉ một thành công; cùng tài khoản không vượt per-user limit |
| Admin/media | Tạo danh mục/sản phẩm/voucher; chỉnh kho/ẩn bánh; chặn xóa danh mục có bánh; xóa mã giảm giá chưa dùng; tải lên và trả ảnh PNG |
| Tải lên lỗi | Chặn nội dung script giả ảnh, MP4 sai chữ ký và đường dẫn traversal |
| MVC thanh toán | Xem trước mã giảm giá, tạo đơn qua POST, xóa giỏ, từ chối token cũ, hiển thị lỗi validation |

Xem kết quả chạy chính xác trong `target/surefire-reports/*.txt` và `target/verify.log`. Kiểm thử chạy bằng JDK 23 tại môi trường phát triển, compiler target/release là Java 17. Không khẳng định đã kiểm thử trực tiếp bằng mọi phiên bản JDK.

## Giới hạn đã biết của lần nghiệm thu này

- Chưa có kiểm chứng trên MySQL Server thật; H2 không chứng minh tương đương hoàn toàn về DDL, collation, isolation hay khóa của MySQL.
- Đã kiểm tra render HTML trên server bằng MockMvc, nhưng chưa nghiệm thu bằng ảnh chụp trình duyệt: quyền mở bản xem thử cục bộ bị từ chối. Không coi render server là đã kiểm tra responsive/CSS/JS/video.
- Kết nối CDN, tải lên MP4 có nội dung phát được, trải nghiệm mobile và luồng Eclipse cần kiểm tra thực tế theo danh sách kiểm tra dưới đây.

## Danh sách kiểm tra MySQL và Eclipse

1. Import cấu trúc bảng và dữ liệu mẫu vào MySQL 8+ mới. Kiểm tra có 8 bảng, 3 danh mục, 6 sản phẩm, 2 voucher.
2. Import Maven project trong Eclipse, chọn JDK 17/21, đặt biến môi trường DB/admin. Khởi động không có lỗi `Schema-validation`.
3. Đăng ký hai khách, đăng nhập admin. USER truy cập `/admin` phải bị từ chối. Logout phải qua POST.
4. Khách lưu địa chỉ, thêm 1 bánh 320.000 đ, dùng WELCOME10: giảm 32.000 đ + ship 30.000 đ → tổng 318.000 đ.
5. Đơn 2 bánh 320.000 đ: giảm tối đa 50.000 đ, miễn ship → tổng 590.000 đ.
6. Kiểm tra chi_tiet_don_hang lưu giá cũ sau khi quản trị viên đổi giá sản phẩm; thông tin địa chỉ trên đơn giữ nguyên.
7. Tồn 1 bánh, dùng hai tài khoản/browser cùng thanh toán: một đơn thành công; kho không âm. Lặp lại với mã giảm giá chỉ còn một lượt, các sản phẩm khác nhau.
8. Quản trị cập nhật PENDING → PREPARING → SHIPPING → COMPLETED. Revenue tăng đúng tổng đơn. Không chuyển ngược/nhảy bước.
9. Hủy một đơn PENDING: kho hoàn đúng một lần; mã giảm giá vẫn tính đã dùng. Không xem/hủy đơn tài khoản khác.
10. Thử mã giảm giá quá hạn, chưa bắt đầu, tắt, min thiếu, hết lượt, per-user hết. Lỗi không lưu đơn và không trừ kho.
11. Thử xóa danh mục có bánh: bị chặn. Ẩn bánh: biến mất khỏi catalog; đơn lịch sử vẫn đọc được. Mở lại bằng sửa sản phẩm.
12. Tải lên ảnh JPG/PNG/WebP và video MP4 thực; reload trang chi tiết, phát video, kiểm tra seek. Tệp quá kích thước/sai loại bị từ chối.
13. Khóa USER đang đăng nhập: request tiếp theo chuyển về đăng nhập. Mở khóa rồi đăng nhập lại được.
14. Đổi tên/mô tả/giới hạn/thời hạn mã giảm giá; mã đã dùng không đổi ma; xóa mã đã dùng chỉ tắt.

## Danh sách kiểm tra giao diện

- Desktop 1440 px và mobile 375 px: đầu trang, menu thu gọn, chân trang, card bánh, bộ lọc, thanh toán không tràn ngang.
- Kiểm tra chữ tiếng Việt, định dạng VND, trạng thái hết hàng và các danh sách rỗng.
- Nút thêm/sửa/xóa giỏ; link danh mục; phân trang giữ bộ lọc; thông báo lỗi giữ form phù hợp.
- AdminLTE sidebar thu gọn; bảng dài có cuộn ngang; tất cả form có nhãn và nút lưu.
- Mất kết nối CDN có thể làm mất định dạng; muốn demo offline thì lưu thư viện local như README.

## Giới hạn nghiệp vụ cần trình bày khi báo cáo

Chuyển khoản và hoàn tiền được xử lý thủ công, không phải cổng thanh toán. Chưa có lịch sử điều chỉnh kho, đơn chờ không tự hết hạn, giỏ không đồng bộ đa thiết bị. Ảnh dữ liệu mẫu là minh họa; cần thay ảnh/video thực cho sản phẩm kinh doanh. Chính sách không hoàn lượt mã giảm giá khi hủy là quyết định MVP có chủ đích, đã hiển thị ở checkout.
