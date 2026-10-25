# Kết quả kiểm thử Bóng Bakery — 25/10/2026

Đã chạy theo yêu cầu của chủ dự án: **35 kiểm thử Java/Spring và 4 kiểm tra JavaScript thành công**, không có bài thất bại hoặc bị bỏ qua trong lượt cuối.

## Các luồng đã kiểm tra

- Đăng ký, đăng nhập bằng mật khẩu mã hóa, phân quyền USER/ADMIN, CSRF, khóa tài khoản, quyền sở hữu đơn và địa chỉ.
- Đăng nhập luôn về `/`, kể cả khi session cũ đã lưu yêu cầu `/sw.js`; yêu cầu nền mới không được ghi vào bộ nhớ chuyển hướng.
- Trang chủ, danh sách bánh, tìm/lọc/phân trang, trang thông tin, chi tiết bánh, giỏ và các màn quản trị qua MockMvc/Thymeleaf.
- Gợi ý tìm kiếm chỉ trả bánh đang bán có tên phù hợp; có tên, giá, mô tả, không trả thông tin riêng; xử lý từ khóa trống, ký tự `%` và từ khóa quá dài.
- Đặt hàng, giá được lưu tại thời điểm mua, voucher, phí giao hàng, chống gửi đơn trùng, hoàn tác khi thất bại, giới hạn tồn kho và tranh mua sản phẩm/voucher cuối cùng.
- Nhận tại cửa hàng: không cần địa chỉ giao, phí giao bằng 0, cần tên/điện thoại; tiến trình đi qua Sẵn sàng nhận bánh, không được chuyển nhầm sang Đang giao.
- Phụ kiện: giá/tồn do máy chủ tính, chỉ giảm giá tiền bánh, lưu đúng từng dòng, hoàn kho một lần khi hủy; thiếu phụ kiện hoàn tác cả đơn và thông báo.
- Đặt trước: ngày 90 được chấp nhận khi bật đặt trước, ngày 91 bị từ chối.
- Thông báo gửi riêng cho khách và Admin; xem/đánh dấu đọc không tác động tài khoản khác, không mở được thông báo của người khác.
- Mua lại: đúng chủ đơn, đưa bánh vào giỏ theo giá hiện tại, không tự tạo đơn, bỏ qua bánh đã ngừng bán.
- Bánh thiết kế riêng: gửi yêu cầu kèm ảnh, chống gửi lặp, trang khách/Admin, báo giá, khách đồng ý, chuẩn bị, sẵn sàng và hoàn thành; kiểm tra quyền sở hữu và chặn chuyển bước sai. Doanh thu bánh riêng được tính khi hoàn thành.
- Đánh giá: chỉ khách đã hoàn tất mua hàng, sao/nội dung/ảnh hợp lệ, không gửi trùng, không tạo trùng khi gửi đồng thời; ẩn/hiện ảnh hưởng đúng điểm trung bình.
- Tải ảnh/video, giới hạn/chữ ký tệp, bộ ảnh bánh, ngăn đường dẫn vượt thư mục.
- JavaScript cục bộ: cú pháp các tệp, nút đóng thanh ưu đãi và lưu lựa chọn trong tab, menu Admin đánh dấu mục chính xác, đổi trường giao/nhận và giới hạn ngày khi bật đặt trước.

## Lỗi đã sửa trong đợt kiểm thử

Cấu hình ép UTF-8 trước đây áp dụng cả cho phản hồi ảnh, làm `Content-Type` thành `image/png;charset=UTF-8`. Đã tách cấu hình ép mã hóa yêu cầu khỏi phản hồi, giữ đúng `image/png`; nội dung HTML vẫn dùng UTF-8 của Thymeleaf.

Bài kiểm tra hồi quy đăng nhập được điều chỉnh để đọc session mới sau khi Spring Security đổi session, thay vì đọc session cũ đã bị vô hiệu hóa. Chức năng bảo vệ session được giữ nguyên.

## Phạm vi và giới hạn

- Kiểm thử ghi dữ liệu dùng H2 trong bộ nhớ ở chế độ SQL Server và thư mục `target/test-uploads`, không ghi vào tài khoản/đơn hàng thật.
- Bản chạy riêng cổng 18081 đã khởi động với SQL Server thật trên SPQR; Hibernate xác nhận cấu trúc bảng và ứng dụng khởi động thành công. Bản chạy riêng được dừng sau kiểm tra.
- Trình duyệt không được cấp quyền truy cập bản chạy riêng. Vì vậy **chưa xác nhận trực quan bằng trình duyệt**, chưa có kiểm tra bố cục thực tế trên điện thoại hoặc ảnh chụp giao diện. Các kiểm tra JavaScript dùng môi trường cục bộ tối giản, không thay thế trình duyệt thực.
- Kiểm thử nghiệp vụ chạy trên H2 không thay thế hoàn toàn kiểm thử giao dịch/cạnh tranh trên một database SQL Server thử riêng.

## Chạy lại

Java/Spring: `mvnw.cmd test` (JDK 17 trở lên; lần chạy này dùng JDK 23).

JavaScript: `node --test src/test/js/ui-behavior.test.cjs`.

Báo cáo Maven: `target/surefire-reports`. Nhật ký lượt cuối: `target/kiem-thu-cuoi.log`; nhật ký khởi động SQL Server: `target/kiem-thu-sql-server.log`.
