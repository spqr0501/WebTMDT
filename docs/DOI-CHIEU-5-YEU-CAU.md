# Đối chiếu 5 yêu cầu thương mại điện tử

Cập nhật 25/10/2026 sau khi bổ sung đổi trả, trợ lý hội thoại, phiếu hỗ trợ và nút tròn ở góc phải dưới. 57 bài Java/Spring và 10 bài JavaScript đạt. SQL Server thật đã nâng cấp file 09 và Spring Boot khởi động với Hibernate validate thành công.

## Kết luận

Đã có triển khai cơ bản cho cả 5 nhóm trong phạm vi đồ án. Nhóm 4 là **hội thoại truy xuất theo quy tắc và gợi ý sản phẩm**, không phải AI tạo sinh/LLM. Nếu tiêu chí môn học bắt buộc một mô hình AI thực sự thì phần kết nối mô hình vẫn cần làm thêm; giao diện hiện công bố đúng giới hạn này.

| Nhóm | Đã có | Giới hạn |
|---|---|---|
| 1. Voucher | Điều kiện tiền bánh, thời hạn, lượt chung/cá nhân, trần, thông báo không hợp lệ, quy tắc cộng gộp | Một mã/đơn; được hưởng cùng miễn phí giao hàng khi đủ ngưỡng, chưa có bộ máy sale riêng |
| 2. Hủy / đổi trả / hoàn tiền | Hủy PENDING, hoàn kho; khách gửi hồ sơ/ảnh; admin duyệt hoặc từ chối có lý do; đổi bánh/làm lại; hoàn tiền từng phần, lịch sử và thông báo | Cửa hàng thực chuyển tiền/làm bánh rồi ghi nhận, không tự gọi ngân hàng; một hồ sơ mỗi đơn |
| 3. Kho / giao nhận | Giữ hàng bằng trừ kho, tranh kho/thiếu hàng rollback; trạng thái và thông báo; cảnh báo kho thấp/hết có nút lọc; duyệt mẫu, QR bàn giao, lịch sản xuất | Chưa tự hết hạn giữ hàng, kho nguyên liệu, công suất bếp hay hãng giao hàng |
| 4. Hội thoại / gợi ý | Nhớ loại/giá trong phiên, nguồn và thời điểm đọc; giới hạn ngoài phạm vi; phiếu chuyển nhân viên có đồng ý chia sẻ, trả lời hai chiều; nút tròn toàn cửa hàng | Dùng quy tắc, không LLM. Tìm tối đa 200 bánh đang bán mới nhất, 4 kết quả, giữ 12 lượt |
| 5. Dashboard | Doanh thu sau hoàn thực tế, trung bình đơn, tỷ lệ hủy, bộ lọc tháng, chờ xử lý/bán chạy/kho, giải thích cách tính và quyết định | Không tính lợi nhuận vì chưa có giá vốn; doanh thu theo tháng tạo đơn, không phải tháng nhận tiền |

## 1. Voucher

Quy tắc chi tiết ở VOUCHER.md. Tiền bánh quyết định mức tối thiểu và giảm giá, không tính phụ kiện/phí giao. Một mã/đơn, không cộng gộp mã. Miễn phí giao khi tiền bánh từ 500.000 đ trước giảm; nhận tại quầy phí giao 0. Xem trước không giữ lượt; tạo đơn khóa và kiểm tra lại kho/giá/mã trong cùng giao dịch. Lỗi thì rollback, hủy không hoàn lượt. Có thông báo mã không tồn tại, tắt, chưa bắt đầu/hết hạn, hết lượt hoặc chưa đạt tối thiểu.

## 2. Hủy, đổi trả và hoàn tiền

Hủy: chủ đơn hoặc admin chỉ hủy đơn thường PENDING, hoàn kho bánh/phụ kiện cùng giao dịch, giữ lượt voucher. Bánh riêng có thể tự hủy SENT/QUOTED; sau chốt cần gửi phiếu hỗ trợ. Hủy không tự chuyển tiền hoàn.

Đổi trả: khi chuyển COMPLETED hệ thống ghi giờ bàn giao. Trong 24 giờ, chủ đơn thường/bánh riêng gửi mô tả 20–2.000 ký tự, 1–3 ảnh JPG/PNG (mỗi ảnh 5 MB, 16 triệu điểm ảnh) và mong muốn hoàn tiền hoặc đổi bánh. Một hồ sơ mỗi đơn; gửi lại dẫn về hồ sơ cũ. Đơn cũ thiếu giờ giao hoặc quá hạn dùng phiếu hỗ trợ để xét riêng, không tự bịa ngày cho dữ liệu cũ.

REQUESTED → APPROVED / REJECTED; APPROVED → COMPLETED. Admin đối chiếu ảnh và mẫu, nhập phản hồi/lý do bắt buộc. Duyệt tiền không vượt tiền thực nhận chưa hoàn; cần đối soát tiền trước. Hoàn thực tế ghi từng phần với tham chiếu và mã chống lặp; khóa đơn rồi hồ sơ, không vượt số còn được duyệt hoặc tiền đang giữ. Đủ khoản duyệt thì tự hoàn tất. Đổi bánh: sau làm/giao thay thế, admin xác nhận kèm ghi chú. Không tự nhập bánh thực phẩm lỗi vào kho bán; bánh thay thế xuất theo điều chỉnh kho có lý do, chưa tạo đơn thay thế tự động.

Khách nhận thông báo lúc gửi, duyệt/từ chối và hoàn tiền/bàn giao. Khoản thực trả lưu append vào sổ. Hồ sơ bị từ chối không hoàn tiền và không âm thầm sửa quyết định; bổ sung thông tin qua phiếu hỗ trợ. Ảnh lưu uploads/private, URL ảnh kiểm tra chủ hồ sơ/admin và trả no-store, rollback sẽ dọn tệp đã lưu.

Tiền ròng là số cửa hàng còn giữ sau hoàn; tổng đã hoàn lưu riêng. Còn phải trả = max(0, tổng đơn − tiền ròng − tổng đã hoàn), không đòi khách trả lại phần đã hoàn. Hóa đơn giữ tổng đơn gốc, hiển thị khoản hoàn và lịch sử. Doanh thu đơn hoàn thành trừ khoản hoàn thực tế; đơn hủy không tính. Chính sách công khai: /policies#returns.

## 3. Kho, sản xuất, giao nhận

Giỏ không giữ kho; tạo đơn trừ lượng có thể bán, hủy hoàn kho. Khóa sản phẩm theo thứ tự ID và giao dịch nguyên khối chống tranh hàng cuối. Chưa tự hết hạn đơn chưa trả tiền.

Sắp hết = 1–4, hết = 0, chỉ bánh đang bán. Thông báo khi đủ hàng xuống sắp hết, có hàng xuống 0, hoặc bổ sung từ 0 lên 1–4; không lặp mỗi lần giảm trong vùng 1–4. Kho và cảnh báo cùng giao dịch. Thông báo mở đúng bộ lọc, dashboard có nút sắp hết/hết hàng.

Giao tận nơi: PENDING → CONFIRMED → PREPARING → SHIPPING → COMPLETED. Nhận tại quầy dùng READY_FOR_PICKUP thay SHIPPING. Các bước có thông báo trên website, chưa đồng bộ hãng giao hàng.

Mẫu bánh riêng lưu từng phiên bản; chỉ mẫu mới nhất được phản hồi. Mẫu mới cần duyệt lại. Bắt đầu làm cần chốt báo giá, duyệt mẫu mới nhất và đủ tiền trả trước. QR sinh nội bộ có HMAC; admin tải ảnh/dán mã, kiểm tra đúng người/tiền rồi bấm bàn giao khi sẵn sàng và trả đủ, quét lặp không bàn giao lần hai. QR không thu tiền.

Lịch /admin/production lọc 1–31 ngày, mặc định 7 ngày/chưa hoàn thành. Gồm bánh/biến thể/số lượng, phụ kiện, lời chúc, giờ hẹn, hình thức nhận và tiền; tổng bánh theo ngày. PENDING là dự kiến, bánh riêng vào lịch sau ACCEPTED. Có thể gồm đơn hoàn thành, không gồm hủy/từ chối. Xem lịch không đổi kho/trạng thái.

## 4. Trợ lý và chuyển hỗ trợ

Trợ lý truy xuất theo quy tắc, không gọi mạng/mô hình ngoài và không tự nhận là AI tạo sinh. Chỉ dùng sản phẩm đang bán (giá cơ bản, kho, tên/danh mục, mô tả, thành phần/dị ứng/bảo quản admin nhập) và StorePolicyService dùng chung để dựng /policies. Không tra người mua khác hoặc bí mật; hỏi đơn cá nhân thì hướng về /orders, nơi kiểm tra chủ đơn.

Nhớ loại/tên bánh và ngân sách như 500k, 0,5 triệu hoặc 500000; chọn tối đa 4 bánh còn hàng, giá tăng. Giá chưa gồm lựa chọn/phụ kiện/phí giao; kiểm tra lại kho khi đặt. Hết hàng phải nêu rõ. Thiếu thông tin hoặc nhiều bánh khi hỏi dị ứng thì hỏi rõ/chuyển nhân viên, không suy đoán công thức, chẩn đoán hoặc bảo đảm an toàn. Không thực thi chỉ dẫn lấy bí mật/chạy lệnh trong câu hỏi.

Mỗi trả lời có nguồn sản phẩm/chính sách và thời điểm đọc. Ngoài phạm vi thì nêu rõ và có nút hỗ trợ. Giới hạn 600 ký tự/câu, 12 lượt trong phiên gắn tài khoản, đổi tài khoản không dùng lại ngữ cảnh. Trang /assistant và khung nổi dùng chung hội thoại. Nút tròn ở footer dùng chung, bấm mở khung ngay trên trang, có câu hỏi nhanh/nguồn/giá/ảnh; Esc hoặc nút đóng, xử lý lỗi mạng/hết phiên và chống gửi trùng. Khách chưa đăng nhập thấy hướng dẫn đăng nhập, không gọi lịch sử riêng.

Phiếu hỗ trợ có chủ đề, nội dung và tùy chọn **mặc định không chia sẻ** hội thoại. Khi khách chọn đồng ý, máy chủ lấy tối đa 6.000 ký tự từ chính phiên đó kèm nguồn. Admin được thông báo, trả lời và chọn OPEN/IN_PROGRESS/RESOLVED; khách bổ sung mở lại OPEN. Chỉ chủ phiếu/admin xem, nội dung được escape, có phân trang. Đây là hộp thư trên website, không giả vờ có người trực tuyến ngay.

## 5. Dashboard

| Chỉ số | Cách tính / bộ lọc | Quyết định |
|---|---|---|
| Doanh thu hoàn thành | Tổng đơn thường sau giảm gồm phụ kiện/phí giao − thực hoàn; cộng báo giá bánh riêng hoàn thành − thực hoàn | Theo quy mô bán sau hỗ trợ |
| Tháng/ngày | Đơn/yêu cầu tạo trong tháng chọn và hiện COMPLETED; gom theo ngày tạo, hoàn sau điều chỉnh lại tháng tạo | So nhịp bán, không thay kế toán dòng tiền |
| Trung bình đơn | Doanh thu tháng / số COMPLETED trong tập; chưa có = 0 | Combo, ngưỡng ưu đãi |
| Tỷ lệ hủy | CANCELLED / tổng đơn + yêu cầu tạo trong tháng × 100%; REJECTED không tính hủy | Tìm nguyên nhân hủy |
| Chờ xử lý | PENDING, SENT, đổi trả REQUESTED, hỗ trợ OPEN hiện tại | Ưu tiên chăm sóc |
| Bán chạy | Top 5 tên bánh theo số lượng đơn thường COMPLETED, chưa trừ số bánh đổi trả, không gồm phụ kiện/bánh riêng | Kế hoạch làm bánh |
| Kho thấp/hết | Bánh đang bán 1–4/0 hiện tại | Bổ sung, ẩn/hiện |
| Lịch sản xuất | Ngày nhận, trạng thái, tối đa 31 ngày | Chia việc theo giờ |

## Triển khai và kiểm thử

Database cũ chạy 08 rồi 09; file 01 đã gồm mọi nâng cấp cho cài mới. SPQR/cake_shop đã chạy 09. Không chạy lại file 01 lên database đang dùng. Khởi động lại Spring Boot sau cập nhật. Xem DOI-TRA-VA-TRO-LY.md và KET-QUA-KIEM-THU-DOI-TRA-HO-TRO.md.
