# SQL Server tổng chính thức — Bóng Bakery

Dùng duy nhất [BONG_BAKERY_SQL_SERVER.sql](BONG_BAKERY_SQL_SERVER.sql), chạy toàn bộ bằng F5 trong SSMS. File tạo 26 bảng, 13 danh mục, 39 bánh có ảnh, 5 tài khoản, 5 đơn mẫu, 2 yêu cầu bánh riêng và voucher.

- Máy chưa có database `cake_shop`: giữ `@XoaDuLieuCu=0`.
- Muốn xóa dữ liệu cũ và tạo lại: **dừng Spring Boot**, đổi `DECLARE @XoaDuLieuCu BIT=0` ở đầu file thành `=1`, rồi chạy cả file. **Toàn bộ database cũ sẽ bị xóa**, không có bản sao lưu tự động. Ảnh trên ổ đĩa không bị xóa.
- Nếu database đã tồn tại và giữ `=0`, file dừng, không nạp đè mẫu vào dữ liệu cũ.
- Không chạy các phần riêng lẻ; không chạy file lịch sử trong `LUU_TRU_SQL_CU_2026-10-28.zip`.
- Các phần 15/16/17 lần lượt gồm Stripe, tọa độ bánh riêng và bộ mẫu thực hành. SQL không chứa khóa API Stripe.

Tài khoản mẫu: `admin@gmail.com`, `khach1@gmail.com`, `khach2@gmail.com`, `shipper1@gmail.com`, `shipper2@gmail.com`. Mật khẩu chung **123456**, đã mã hóa BCrypt.

Xem [tài khoản, voucher và các tình huống thực hành](../docs/DU-LIEU-MAU-VA-TINH-HUONG.md). Đã tạo lại bộ mẫu trên SPQR ngày 29/10/2026 và xác nhận website khởi động với kiểm tra cấu trúc SQL Server; đã kiểm thử các luồng HTTP trên bộ mẫu.

Phần 18 bổ sung dữ liệu đối soát hoàn thực tế. Phần 19 bổ sung giỏ tài khoản, địa chỉ mặc định và lựa chọn nhận gần nhất. Khách 1/2 có một bánh trong giỏ mẫu; Khách 1 được gợi ý nhận tại quầy 13:00–17:00, Khách 2 giao tận nơi cùng khung giờ.
