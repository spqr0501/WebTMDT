# Cơ sở dữ liệu chính thức — Bóng Bakery

Chỉ có **một file SQL chính thức**: [BONG_BAKERY_SQL_SERVER.sql](BONG_BAKERY_SQL_SERVER.sql).

File dành cho **Microsoft SQL Server**, tạo database `cake_shop`, 25 bảng cùng các ràng buộc/chỉ mục và dữ liệu mẫu: **13 danh mục, mỗi danh mục 3 bánh**, tổng cộng 39 bánh có ảnh và mô tả tiếng Việt. Không có bánh trung thu. Đã tích hợp toàn bộ phần nâng cấp đến ảnh sản phẩm, yêu thích, quên mật khẩu và **phần 13: bản đồ địa chỉ, shipper, chia sẻ GPS** và **phần 14: số điện thoại cá nhân**, **phần 15: Stripe Sandbox**.

## Cách dùng

1. Mở SQL Server Management Studio, kết nối máy chủ `SPQR` bằng Windows Authentication.
2. Khi cài mới và chưa có bảng của website, mở `BONG_BAKERY_SQL_SERVER.sql`.
3. Chọn toàn bộ nội dung và nhấn F5. Chạy cả file, không chỉ riêng đoạn đang bôi đen.
4. Khởi động website bằng Spring Boot App trong Eclipse.

File sử dụng `GO` của SSMS, không dán vào trình chạy SQL của MySQL. Ảnh nằm trong `src/main/resources/static/images/banh/`, nguồn tại `/image-credits`. SQL không tự tạo mật khẩu quản trị; tài khoản được quản lý bởi ứng dụng.

**Database trên máy SPQR đang có dữ liệu và đã được nâng cấp: không cần chạy lại file tạo mới.** Lần hợp nhất trước không đổi dữ liệu. Phần 13 bản đồ/shipper, phần 14 trang cá nhân và phần 15 Stripe Sandbox hiện đã được áp dụng trên SPQR; không cần chạy lại. Máy khác có database cũ chỉ chạy các phần còn thiếu theo thứ tự (13, 14 rồi 15), sau những nâng cấp trước đó.

## Các bản cũ

`LUU_TRU_SQL_CU_2026-10-28.zip` giữ nguyên 12 file SQL trước khi hợp nhất, gồm các bản MySQL cũ và các phần nâng cấp SQL Server. Chỉ dùng để tra cứu lịch sử hoặc phục hồi hướng dẫn nâng cấp cụ thể, không chạy tất cả file trong ZIP. Các hướng dẫn cũ nhắc số file 04–12 là tên lịch sử trong ZIP; cài mới chỉ cần file chính thức ở trên.
