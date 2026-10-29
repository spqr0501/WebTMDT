# Giỏ tài khoản, lịch nhận và hoàn tiền

## Giỏ hàng

- Khách chưa đăng nhập: giỏ nằm trong session của trình duyệt.
- Khách đăng nhập: giỏ lưu trong bảng `dong_gio_hang`, mở thiết bị khác đăng nhập vẫn tải lại được.
- Đăng nhập thành công gộp giỏ khách và giỏ tài khoản một lần; cộng số lượng cùng lựa chọn rồi giới hạn theo tồn thực tế, tối đa 99 mỗi dòng, 50 dòng. Có cảnh báo khi điều chỉnh.
- Giá không được chốt hoặc giữ tồn trong giỏ. Khi đặt, máy chủ kiểm tra lại giá/tồn trong giao dịch khóa kho.
- Cập nhật giỏ trên nhiều thiết bị khóa theo tài khoản để tránh ghi đè thao tác thêm. Khi đặt thành công, chỉ trừ lượng đã mua khỏi giỏ, giữ lượng mới thêm trong lúc đặt.
- Đổi tài khoản hoặc trở về khách không lộ giỏ của người dùng trước. Đăng xuất không xóa giỏ đã lưu trong database.

## Mặc định và ghi nhớ

- Sổ địa chỉ có nút Đặt làm mặc định và ô chọn trong biểu mẫu. Địa chỉ đầu tiên tự làm mặc định; xóa mặc định sẽ chọn một địa chỉ còn lại.
- Mỗi tài khoản tối đa một địa chỉ mặc định, có chỉ mục SQL Server và khóa cập nhật theo tài khoản.
- Sau khi đặt thành công, lưu hình thức nhận và khung giờ gần nhất vào tài khoản. Lần tiếp theo gợi ý lại; ngày nhận luôn là ngày mới hợp lệ. Không nhớ mã giảm giá hoặc lựa chọn thanh toán cũ để tránh áp dụng nhầm.
- Lịch giao diện tự dựng bằng JavaScript, hiển thị ngày/tháng/năm và tháng tiếng Việt, không phụ thuộc định dạng ngày của trình duyệt. Giới hạn ngày được kiểm tra lại trên máy chủ theo giờ Việt Nam.

## Hoàn tiền bắt buộc sau hủy

- Đơn đã hủy mà `da_thanh_toan > 0` tự nằm ở `/admin/refunds`, có số đếm và cảnh báo dashboard. Hoàn một phần vẫn giữ trong danh sách cho đến khi số cần hoàn bằng 0.
- Khách xem trạng thái chờ hoàn, số cần hoàn và lịch sử ngay trong chi tiết đơn. Hủy chỉ trả kho, không tự chuyển tiền.
- Trước khi ghi sổ, admin phải chọn kênh hoàn, nhập nơi nhận (ngân hàng/tài khoản/tên, hoặc thẻ gốc/mã thanh toán Stripe), mã giao dịch thực tế và đánh dấu đã xác minh hoàn thành công.
- Hỗ trợ ghi nhận hoàn chuyển khoản hoặc khoản đã hoàn trên Stripe Dashboard. **Chưa gọi Stripe Refund API hoặc ngân hàng tự động.** Admin chịu trách nhiệm đối chiếu giao dịch thực tế trước khi xác nhận; không lưu đầy đủ số thẻ/CVC.
- Máy chủ bắt buộc xác nhận và đủ thông tin. Không vượt tiền còn giữ/mức duyệt đổi trả. Mã giao dịch hoàn duy nhất trong từng kênh trên toàn hệ thống; có chỉ mục SQL để chống ghi lặp đồng thời. Gửi lại cùng mã xác nhận, cùng nội dung không hoàn hai lần.
- Đơn không thanh toán Stripe không được ghi kênh hoàn Stripe. Nếu hoàn Stripe bằng chuyển khoản thỏa thuận, cần đối chiếu cả hai bên để không hoàn lặp qua hai kênh.
- Các bản ghi hoàn cũ giữ nguyên, không tự coi đã được xác minh lại.

Cấu trúc mới ở phần 18–19 của SQL tổng. Sau đợt kiểm thử, database thật được nạp lại bộ mẫu sạch; không có giao dịch Stripe thật hoặc chuyển tiền ngân hàng nào được thực hiện.
