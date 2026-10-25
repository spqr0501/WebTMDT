# Kết quả kiểm thử ngày 25/10/2026

## Kết quả

- 73 kiểm thử Java/Spring: 72 trong CakeShopIntegrationTests và 1 khởi động ứng dụng. Không lỗi, không bỏ qua.
- 15 kiểm thử JavaScript: đạt toàn bộ.
- Khởi động trực tiếp với SQL Server SPQR, Windows Authentication và `ddl-auto=validate`: thành công. Dùng cổng 8081 riêng, đã dừng tiến trình kiểm tra sau khi hoàn tất.
- 55 lượt HTTP kiểm tra: thành công, gồm trang chủ, bộ sưu tập, bộ lọc, chi tiết 4 bánh, gợi ý tìm kiếm, nguồn ảnh, 39 ảnh, đăng nhập, trang admin, báo cáo theo thời gian, nhập Excel, phụ kiện, yêu thích và PDF báo cáo.
- Đăng nhập quản trị thành công. PDF báo cáo bộ lọc 01/10–25/10/2026 tải thành công (13.544 byte).
- SQL Server: 13 danh mục × 3 bánh đang bán = 39 bánh; 39 đường dẫn ảnh mới đã được cập nhật. Không thêm bánh trung thu.

## Nội dung được kiểm tra tự động

Báo cáo có kiểm tra bộ lọc, thời gian, doanh thu ròng và PDF. Excel có kiểm tra file mẫu, thêm bánh/phụ kiện, điều chỉnh kho, dòng lỗi/công thức, hủy toàn bộ lần nhập khi dữ liệu thay đổi, CSRF và gửi xác nhận trùng. Yêu thích có kiểm tra riêng tư giữa tài khoản, lưu lặp, bánh ẩn và không giữ tồn. Xóa phụ kiện có kiểm tra giữ lịch sử đơn.

Quên mật khẩu có kiểm tra SMTP tắt, gửi thư giả lập, mã băm, hết hạn, dùng một lần, giới hạn gửi lại, nhập lại mật khẩu và hủy phiên cũ. Không gửi email thật vì chủ dự án sẽ cấu hình SMTP sau.

Ảnh có kiểm tra đủ 39 file độc lập, đọc được bằng bộ giải mã ảnh, mã SHA-256 khớp bảng nguồn, có tác giả/giấy phép và mô tả tiếng Việt; trang nguồn công khai, ảnh trả về đúng định dạng. Ghi nguồn dưới ảnh sản phẩm xuất hiện đúng khi dùng ảnh mẫu và không gắn nhầm vào ảnh admin upload. Đã xem ảnh các nhóm dễ nhầm để chọn đúng loại và hương vị minh họa.

## Phạm vi

Kiểm thử nghiệp vụ dùng H2 chế độ SQL Server và MockMvc. Trên SQL Server thật chỉ cập nhật dữ liệu mẫu bằng các file nâng cấp và kiểm tra đọc trang/ảnh/báo cáo; không tạo đơn hoặc gửi yêu cầu hoàn tiền thử trên dữ liệu thật. Chưa nghiệm thu trực quan toàn bộ trang bằng trình duyệt; kết quả HTTP không thay thế kiểm tra bố cục responsive. Không xác nhận email thực tế khi SMTP chưa được cấu hình.

Log: `target/kiem-thu-bo-sung-va-anh.log`, `target/surefire-reports/`, `target/kiem-thu-http-anh.json`, `target/anh-sql-startup.log`. Các tệp trong target là kết quả chạy cục bộ và có thể bị xóa khi clean project.
