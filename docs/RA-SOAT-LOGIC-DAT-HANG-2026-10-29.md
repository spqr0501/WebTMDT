# Rà soát logic đặt hàng – 29/10/2026

Đợt này rà soát mã nguồn và sửa lỗi; chưa chạy kiểm thử tự động, kiểm thử trên trình duyệt hoặc giao dịch Stripe. Biên dịch không xác nhận toàn bộ nghiệp vụ đã hoạt động đúng.

## Các lỗi đã sửa

- Checkout: số điện thoại nhận tại cửa hàng và địa chỉ mới dùng định dạng di động Việt Nam 10 số. Chuẩn hóa +84/84 và dấu phân cách. Địa chỉ cũ có SĐT không hợp lệ bị chặn khi đặt và hướng dẫn sửa, không âm thầm sửa thông tin đơn đã đặt.
- Áp dụng kiểm tra SĐT cho sổ địa chỉ và trang cá nhân để không tiếp tục lưu dữ liệu sai. Chưa xác thực số điện thoại bằng OTP.
- Ngày nhận thanh toán hiển thị ngày/tháng/năm, phân tích ngày nghiêm ngặt (không nhận 31/02), tính giới hạn theo múi giờ Việt Nam: đơn thường ngày mai–30 ngày; đặt trước ngày mai–90 ngày. Vẫn hỗ trợ dữ liệu ISO từ trang cũ.
- Checkout dùng địa chỉ mới có bản đồ, lấy vị trí và tự tìm ghim khi dừng nhập. Tọa độ là tùy chọn; phải kiểm tra vị trí gợi ý trước khi đặt. Nhận tại cửa hàng bỏ qua địa chỉ giao.
- Hoàn thành mọi đơn, gồm COD giao tận nơi và bánh riêng: phải ghi nhận đủ tiền. Với đơn thường giao tận nơi, shipper phải báo bàn giao trước khi admin hoàn tất. Báo giao không tự ghi nhận đã thu COD.
- Hạn đổi trả 24 giờ lấy mốc bàn giao của shipper nếu có, thay vì mốc admin hoàn tất muộn. Đơn cũ dùng thời điểm hoàn thành dự phòng; thiếu cả hai mốc thì chuyển hỗ trợ. Không nhận mốc bàn giao trong tương lai.
- Bấm hủy lại đơn đã hủy không hoàn kho thêm lần nữa và không tạo thông báo trùng.
- Ghi nhận thu/hoàn: cùng mã tham chiếu và cùng loại giao dịch trên một đơn không được cộng lại bằng mã gửi mới. Mã gửi cũ với cùng nội dung vẫn an toàn khi thử lại. Các khoản thu/hoàn khác nhau cần mã tham chiếu riêng; quy tắc này áp dụng trong từng đơn.
- Giỏ hàng được làm sạch khi bánh bị xóa/ngừng bán và có cảnh báo.
- Giá trị phương thức thanh toán không hợp lệ không làm vỡ trang hiển thị lỗi.
- Thông báo sau khi đồng ý báo giá bánh riêng nói rõ còn bước duyệt mẫu và thanh toán trước khi làm.

## Quy tắc được đối chiếu trong mã

| Luồng | Quy tắc |
|---|---|
| Đặt hàng | Máy chủ tính lại giá bánh, lựa chọn, phụ kiện, voucher và phí giao. Không nhận tổng tiền do trình duyệt gửi. |
| Tồn kho | Khóa sản phẩm theo thứ tự ID, kiểm tra tồn và trừ kho trong giao dịch; lỗi thanh toán/voucher/phụ kiện làm hủy giao dịch. Phụ kiện cũng được khóa khi trừ/hoàn. |
| Gửi lặp | Mã checkout gắn tài khoản; khóa tài khoản khi tạo đơn để chống cùng yêu cầu tạo nhiều đơn. |
| Voucher | Một mã/đơn, còn hiệu lực/lượt, đạt tối thiểu; chỉ giảm tiền bánh, không phụ kiện/phí giao; hủy không hoàn lượt. |
| Nhận cửa hàng | Không phí giao, chuyển khoản hoặc Stripe; cọc 50% (làm tròn lên nguyên đồng) hoặc trả đủ; đủ khoản trả trước mới xác nhận/làm. |
| Giao tận nơi | COD trả khi nhận; chuyển khoản/Stripe trả trước đầy đủ mới xác nhận/làm. |
| Hủy | Chủ đơn được hủy lúc chờ xác nhận; hoàn tồn bánh/phụ kiện trong giao dịch. Đơn đã xác nhận cần liên hệ hỗ trợ, không tự chuyển ngược hoặc hoàn kho bánh đã làm. |
| Đổi trả | Chủ đơn đã hoàn thành gửi trong 24 giờ, mô tả 20–2.000 ký tự, 1–3 ảnh JPG/PNG; mỗi đơn một hồ sơ. Admin duyệt/từ chối có phản hồi. |
| Hoàn tiền | Không vượt tiền đã nhận chưa hoàn và mức duyệt còn lại. Duyệt không đồng nghĩa đã chuyển tiền. Đổi bánh không tự nhập bánh cũ vào tồn kho. |
| Stripe | Chỉ webhook đã xác minh mới ghi tiền; không dựa vào trang quay lại. Khoản đến sau khi hủy được đánh dấu đối soát, không mở lại đơn/giữ kho. |

## Phạm vi cần hiểu rõ

- Hoàn tiền là admin ghi nhận sau khi đã chuyển hoàn thực tế, chưa tự gọi Stripe Refund hoặc ngân hàng.
- Đơn chờ chưa thanh toán giữ tồn cho đến khi được hủy; chưa có bộ hẹn giờ tự hủy/nhả tồn. Admin cần xử lý các đơn chờ lâu.
- Mốc giao hàng là shipper báo bàn giao; tranh chấp, đơn quá hạn hoặc thiếu dữ liệu chuyển phiếu hỗ trợ.
- Một hồ sơ đổi trả cho mỗi đơn; sau khi từ chối, trao đổi thêm qua hỗ trợ.
- Đã bổ sung 8 ca hồi quy và cập nhật các bước hoàn tất đơn trong dữ liệu kiểm thử. Chưa chạy nên chưa có kết quả đạt/trượt cho đợt này.
