# Tạo tài khoản quản trị Bóng Bakery trong Eclipse

Tài khoản quản trị hiện tại theo cấu hình đồ án: `admin@gmail.com`, mật khẩu `123456`. Đăng nhập rồi mở `/admin`.

## Tạo thêm quản trị viên

1. Dừng ứng dụng Spring Boot đang chạy.
2. Trong Eclipse, chọn **Run → Run Configurations… → Spring Boot App** rồi chọn cấu hình của Bóng Bakery.
3. Mở thẻ **Environment**, bấm **New…** để thêm hai biến:
   - `ADMIN_EMAIL`: địa chỉ email mới chưa có trong hệ thống.
   - `ADMIN_PASSWORD`: mật khẩu bạn chọn, ít nhất 12 ký tự và tối đa 72 byte UTF-8.
4. Bấm **Apply → Run**. Ứng dụng tạo tài khoản có quyền `ADMIN`, mật khẩu được mã hóa BCrypt.
5. Đăng nhập bằng tài khoản vừa tạo, mở `/admin` để quản lý cửa hàng.
6. Sau khi tạo xong, xóa hai biến khỏi cấu hình chạy nếu không còn cần tạo tài khoản ban đầu.

Nếu email đã tồn tại, ứng dụng bỏ qua: không tự nâng quyền hoặc đổi mật khẩu tài khoản đó. Hãy dùng email mới. Trang đăng ký công khai chỉ tạo khách hàng, không tạo quản trị viên.

## Thông tin cửa hàng

Mặc định: điện thoại **0902592231**; địa chỉ mẫu **123 Nguyễn Thị Minh Khai, phường Bến Thành, TP. Hồ Chí Minh**; giờ nhận bánh hằng ngày **09:00–12:00, 13:00–17:00, 18:00–20:00**. Có thể thay trong Environment qua `SHOP_PHONE`, `SHOP_ADDRESS`, `SHOP_HOURS`.

Địa chỉ trên là dữ liệu mẫu theo yêu cầu đồ án. Bản đồ liên hệ tìm theo địa chỉ đã cấu hình. Khi thay cửa hàng thật, cập nhật lại địa chỉ trước khi nhận khách.

## Cập nhật form bánh riêng

- Số điện thoại: định dạng di động Việt Nam 10 số, đầu 03/05/07/08/09; hỗ trợ chuẩn hóa +84. Đây là kiểm tra định dạng, chưa xác minh sở hữu bằng OTP.
- Ngân sách: dấu chấm phân cách hàng nghìn, từ 10.000 đến 999.999.999 đ.
- Ngày nhận: ngày/tháng/năm, đặt trước 3–90 ngày theo múi giờ Việt Nam.
- Địa chỉ: nhập để tự tìm ghim, chọn trên bản đồ hoặc lấy vị trí hiện tại sau khi cho phép. Kết quả tự tìm là gợi ý, cần kiểm tra số nhà và ghim trước khi gửi. Tọa độ được lưu cùng yêu cầu giao tận nơi; nhận tại cửa hàng bỏ qua tọa độ.
- Chi tiết giao nhận có nhãn người nhận, SĐT, ngày, giờ, hình thức nhận, địa chỉ và thanh toán.

Phần 16 của `database/BONG_BAKERY_SQL_SERVER.sql` bổ sung hai cột tọa độ, không xóa dữ liệu cũ. Máy phát triển đã được cập nhật hai cột; không chạy lại toàn bộ file khởi tạo lên database đang dùng.
