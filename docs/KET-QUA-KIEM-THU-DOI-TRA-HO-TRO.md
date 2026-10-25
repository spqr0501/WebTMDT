# Kết quả kiểm thử đổi trả, trợ lý và khung nổi

Ngày 25/10/2026, sau khi người dùng yêu cầu bổ sung và kiểm thử.

## Kết quả cuối

| Hạng mục | Kết quả |
|---|---|
| Java / Spring / Maven | **57 đạt**, 0 lỗi, 0 thất bại, 0 bỏ qua: 56 nghiệp vụ/giao diện/API + 1 khởi tạo |
| JavaScript cục bộ | **10 đạt**, 0 lỗi: 4 bài cũ + 6 khung trò chuyện nổi |
| SQL Server thật | File 09 áp dụng thành công SPQR/cake_shop, Spring Boot khởi động với Hibernate validate thành công |
| QR | Tạo PNG/giải mã, điều kiện trạng thái/tiền/quyền và quét lặp đạt trong bộ Java |
| PDF | Tạo/đọc nội dung tiếng Việt, khoản hoàn và quyền truy cập đạt trong bộ Java |

Lệnh: `mvnw.cmd -q test`; `node --test src/test/js/ui-behavior.test.cjs src/test/js/assistant-widget.test.cjs`. Java log ở target/kiem-thu-doi-tra-ho-tro.log, báo cáo target/surefire-reports; SQL startup log target/sql-schema-check.log. Tiến trình Java kiểm tra riêng đã dừng, không dừng website người dùng.

## Phạm vi

- Các bài cũ: tài khoản/phân quyền/CSRF, voucher/tranh lượt, giỏ/giá/biến thể/tranh kho, CRUD/upload/đánh giá, địa chỉ, giao nhận, phụ kiện, thông báo, mua lại, bánh riêng.
- Đổi trả: đúng chủ đơn, đã hoàn thành/có giờ bàn giao, quá 24 giờ/đơn cũ, cần ảnh, ảnh lỗi rollback và dọn tệp, một hồ sơ mỗi đơn, không tự hoàn kho thực phẩm.
- Quyết định: admin, phản hồi bắt buộc, không duyệt quá tiền thực nhận, không hoàn hồ sơ từ chối, không sửa quyết định đã xử lý.
- Hoàn: từng phần, đủ thì hoàn tất, chống lặp, vượt duyệt, hai giao dịch đồng thời không vượt; đơn thường/bánh riêng, tiền ròng/tổng hoàn/doanh thu/hóa đơn.
- Đổi bánh: cần duyệt rồi ghi chú bàn giao, không dùng nhánh này để hoàn tiền hoặc cộng kho bánh lỗi.
- Riêng tư: ảnh chủ đơn/admin được xem, người khác không, no-store; trang và thao tác admin được bảo vệ.
- Trợ lý: nhớ loại/giá, dẫn nguồn/thời điểm, không suy đoán dị ứng/bảo quản trống, bánh hết/ẩn, ngoài phạm vi/lấy bí mật, giới hạn lịch sử/câu hỏi, escape HTML, CSRF và tách phiên giữa tài khoản.
- Chuyển hỗ trợ: mặc định không chia sẻ, chọn đồng ý mới có hội thoại/nguồn; thông báo, chống tạo lặp, trả lời hai chiều, đúng chủ/admin, mở lại, render danh sách/chi tiết/bộ lọc.
- Khung nổi Java/API: nút dùng chung trên trang chủ/chi tiết bánh, đăng nhập, tài nguyên công khai, API nguồn/giá, no-store, CSRF và cùng hội thoại phiên.
- Khung nổi JS: mở/đóng/Escape/focus, khách chưa đăng nhập không gọi lịch sử, CSRF, ảnh/giá/nguồn, không chèn HTML hoặc URL javascript, lỗi mạng/hết phiên giữ câu hỏi, chặn gửi trùng, câu hỏi nhanh, trang không có nút vẫn chạy.
- Bổ sung trước: duyệt mẫu mới nhất, mẫu cũ/người khác, QR sẵn sàng/trả đủ/quét lặp, lịch sản xuất, lọc kho, cảnh báo chuyển ngưỡng không lặp.

## Lỗi đã sửa

Lượt đầu 55 bài có 2 lỗi: biểu thức danh sách câu hỏi mẫu Thymeleaf sai khiến trang trợ lý không mở, đã chuyển danh sách sang controller; một bài kiểm thử dùng phiên bản sản phẩm cũ, đã sửa dữ liệu kiểm thử. Chạy lại 55 bài đạt. Sau bổ sung nút nổi, chạy lại toàn bộ 57 bài và 10 JS đạt.

SQL Server thật phát hiện CHECK so sánh hai cột phải đặt ở cấp bảng và cần SET options cho chỉ mục có điều kiện. Đã sửa script, chạy thành công. Hội thoại dài dùng NVARCHAR(MAX), không NVARCHAR(6000).

## Giới hạn

Nghiệp vụ dùng H2 chế độ MSSQL/MockMvc, không tạo đơn hay khoản hoàn thử vào dữ liệu SQL Server người dùng. SQL thật chỉ nâng cấp và kiểm tra khởi động/schema. Chưa kiểm tra bằng trình duyệt thật/camera hoặc bố cục thị giác; PDF kiểm tra nội dung, chưa render từng trang. JS dùng môi trường DOM giả lập, không phải trình duyệt. Không thực chuyển tiền/gọi hãng giao hàng/mô hình AI bên ngoài. Trợ lý hiện theo quy tắc, không AI tạo sinh.
