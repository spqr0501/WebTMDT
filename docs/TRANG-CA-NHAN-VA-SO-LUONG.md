# Trang cá nhân, phân trang và giới hạn số lượng

## Trang cá nhân

Mở **Tài khoản → Trang cá nhân** (`/account`). Có các mục:

- Thông tin cá nhân: sửa họ tên, số điện thoại; email đăng nhập chỉ xem vì chưa có luồng xác minh thay email.
- Sổ địa chỉ giao bánh: thêm, sửa, xóa địa chỉ, giữ chức năng bản đồ/GPS. Mỗi tài khoản tối đa 20 địa chỉ. Người dùng chỉ thao tác địa chỉ của mình; sửa hoặc xóa địa chỉ không thay đổi bản chụp địa chỉ trên đơn đã đặt.
- Đánh giá của tôi: xem bánh, sao, nội dung, ảnh, ngày viết, trạng thái hiển thị và lý do quản trị ẩn nếu có. Danh sách chỉ lấy đánh giá của tài khoản đăng nhập, có phân trang.
- Đổi mật khẩu: cần mật khẩu hiện tại, mật khẩu mới và xác nhận. Có nút mắt hiện/ẩn; đổi thành công hủy các phiên đăng nhập cũ và mã quên mật khẩu chưa dùng, yêu cầu đăng nhập lại.
- Lối tắt đến đơn hàng, yêu thích, bánh riêng, đổi trả, thông báo và hỗ trợ.

Số điện thoại cá nhân được gợi ý cho địa chỉ mới và nhận tại cửa hàng; không tự ghi đè điện thoại trên địa chỉ đã lưu hoặc đơn cũ. Đường dẫn cũ `/account/addresses` chuyển đến sổ địa chỉ trong trang cá nhân. Khi thêm địa chỉ từ thanh toán, lưu thành công quay lại thanh toán.

## Số lượng và tồn kho

Khi nhập vượt giới hạn tại chi tiết bánh, giỏ hàng hoặc phụ kiện thanh toán, giao diện đưa số lượng về mức tối đa và hiện cảnh báo ngay. Máy chủ cũng giới hạn khi thêm/sửa giỏ để trường hợp tắt JavaScript vẫn không tăng vượt tồn. Với nhiều kích thước/hương vị của cùng một bánh, tổng các lựa chọn cùng dùng tồn kho sản phẩm, không tính mỗi lựa chọn là một kho riêng.

Ví dụ bánh còn 5, giỏ có 2 chiếc lựa chọn A thì lựa chọn B chỉ thêm được 3. Nếu tồn giảm khi đang mua, mở giỏ/thanh toán sẽ giảm số lượng về mức còn bán, bỏ dòng hết hàng và báo kiểm tra lại tổng tiền. Khi giỏ có nhiều lựa chọn và tồn bị giảm, hệ thống điều chỉnh theo thứ tự mã lựa chọn, không vượt tổng tồn.

Tối đa 99 bánh cho mỗi dòng và 50 lựa chọn trong giỏ. Giỏ không giữ hàng; bước đặt đơn vẫn khóa và kiểm tra tồn thực tế trong giao dịch. Nếu người khác mua trước, đơn không được tạo thiếu hàng; giỏ được điều chỉnh và hiển thị lỗi/cảnh báo để khách kiểm tra rồi đặt lại. Không tự đặt đơn sau khi điều chỉnh.

## Phân trang

Trang bộ sưu tập có các nút 1, 2, 3… cùng Trước/Sau. Giữ nguyên từ khóa, danh mục, giá và sắp xếp khi chuyển trang. Trang đang xem được đánh dấu; nhiều trang thì dùng cửa sổ số trang và dấu …, vẫn có trang đầu/cuối. Khi đường dẫn trỏ quá trang cuối, trả về trang cuối còn dữ liệu. Đánh giá cá nhân dùng cùng cách đánh số.

## Cơ sở dữ liệu và trạng thái

Đã bổ sung cột `nguoi_dung.so_dien_thoai` trên SPQR, không sửa dữ liệu liên hệ/đơn cũ. File tổng `database/BONG_BAKERY_SQL_SERVER.sql` đã có **phần 14**. Máy SPQR hiện tại không cần chạy lại; database mới dùng cả file, máy cũ chỉ áp dụng phần còn thiếu.

Đã biên dịch bằng Maven, không chạy bộ kiểm thử hoặc trình duyệt trong lần này theo yêu cầu chỉ kiểm thử khi được yêu cầu rõ ràng. Kết quả biên dịch không xác nhận giao diện hoặc luồng thực tế đã nghiệm thu.

## Cập nhật kiểm thử 28/10/2026

Đã chạy kiểm thử theo yêu cầu mới: 87 Java/Spring và 24 JavaScript đạt. Có kiểm tra bản đồ/giới hạn số lượng/phân trang trên trình duyệt. Xem [báo cáo đầy đủ và các giới hạn](KET-QUA-KIEM-THU-2026-10-28.md). Ghi chú chưa kiểm thử bên trên mô tả thời điểm bàn giao trước lượt kiểm tra này.
