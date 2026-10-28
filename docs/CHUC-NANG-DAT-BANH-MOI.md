# Thông báo, phụ kiện và đặt bánh Bóng Bakery

> Cập nhật 28/10/2026: `database/` chỉ dùng một file chính thức `BONG_BAKERY_SQL_SERVER.sql` cho cài mới. Các file nâng cấp riêng được giữ trong `database/LUU_TRU_SQL_CU_2026-10-28.zip`; các số file bên dưới là lịch sử. Database SPQR đã nâng cấp không cần chạy lại.

## Khởi động sau khi cập nhật

Trên máy SPQR, file `database/05-thong-bao-combo-va-banh-rieng.sql` đã được áp dụng để bổ sung cấu trúc. Trong Eclipse, Refresh dự án rồi khởi động lại Spring Boot App. Không cần chạy lại file 01 trên database đang dùng.

Máy khác đang có dữ liệu: chạy file 04 nếu chưa nâng cấp, sau đó file 05. Cài mới dùng file 01 đã bao gồm cả hai phần bổ sung. Các script 04/05 có thể chạy lại và không xóa dữ liệu cũ.

## Thông báo trong website

- Khách nhận thông báo khi đơn được tiếp nhận, xác nhận, chuẩn bị, đang giao hoặc sẵn sàng nhận tại cửa hàng, hoàn thành và hủy.
- Mỗi Admin đang hoạt động nhận thông báo riêng khi có đơn mới, gồm khách đặt, tên bánh, lựa chọn, số lượng, nến/thiệp, ngày nhận và tổng tiền. Admin nhận thông báo khi khách hủy đơn hoặc gửi/chốt yêu cầu bánh riêng.
- Trang Thông báo có số chưa đọc, xem chi tiết và đánh dấu tất cả đã đọc. Mỗi người chỉ đọc/đánh dấu thông báo của mình. Số chưa đọc cập nhật mỗi 30 giây khi tab đang mở; không tự đánh dấu đã đọc.
- Đây là thông báo lưu trong website, chưa gửi email, SMS, Zalo hoặc thông báo đẩy khi đóng trình duyệt. Không tạo thông báo giả cho đơn cũ.

## Giao hàng, nhận tại cửa hàng và đặt trước

- Đơn giao: Chờ xác nhận → Đã xác nhận → Đang chuẩn bị → Đang giao → Hoàn thành.
- Đơn nhận tại cửa hàng: Chờ xác nhận → Đã xác nhận → Đang chuẩn bị → Sẵn sàng nhận bánh → Hoàn thành.
- Khách tự hủy được ở bước Chờ xác nhận. Hủy đơn hoàn lại tồn bánh và phụ kiện; không hoàn lượt mã giảm giá.
- Nhận tại cửa hàng cần tên và điện thoại người đến nhận, không cần địa chỉ giao; phí giao bằng 0. Điền địa chỉ cửa hàng thực tế qua `SHOP_ADDRESS` hoặc `app.shop.address`. Khi chưa cấu hình, giao diện ghi rõ cửa hàng cần xác nhận địa điểm nhận.
- Đơn thường chọn từ ngày mai đến 30 ngày tới. Bật Đặt trước để chọn tối đa 90 ngày. Có ba khung giờ dự kiến như trước.
- Tồn hiện tại được trừ khi đặt đơn thành công, kể cả đơn đặt trước; chưa có mô hình giữ nguyên liệu hoặc năng lực sản xuất theo từng ngày. Admin xác nhận khả năng đáp ứng trước khi chốt đơn.

## Combo bánh cùng nến/thiệp

- Chọn bánh vào giỏ, sau đó chọn phụ kiện tại bước đặt hàng. Đây là mua kèm cùng đơn, không có giảm giá combo tự động.
- Admin → Nến & thiệp: thêm, sửa tên/giá, ngừng bán và nhập/xuất tồn. Tối đa 10 loại phụ kiện đang bán; mỗi loại tối đa 20 món trong một đơn.
- Hai phụ kiện mẫu: Nến sinh nhật 10.000 đ, Thiệp chúc mừng 15.000 đ; tồn mẫu bằng 0. Quản trị viên tự nhập giá/tồn thực tế.
- Lưu lại tên và giá phụ kiện vào đơn. Giá/tồn được kiểm tra lại ở máy chủ khi đặt hàng; nếu thiếu hàng, toàn bộ giao dịch được hoàn tác.
- Mã giảm giá và ngưỡng miễn phí giao 500.000 đ chỉ tính tiền bánh, không tính phụ kiện. Tổng thanh toán = tiền bánh + phụ kiện − giảm giá + giao hàng.
- Sau khi đổi phụ kiện/hình thức nhận/mã ưu đãi, bấm Cập nhật để xem tổng dự kiến mới. Đơn luôn tính lại từ dữ liệu hiện tại lúc xác nhận.

## Mua lại đơn cũ

Mở Đơn hàng → Mua lại hoặc vào chi tiết đơn. Bánh được thêm vào giỏ hiện tại, không tự tạo đơn hay thanh toán. Kiểm tra lại quyền sở hữu đơn, sản phẩm còn bán, lựa chọn đang hoạt động, giá, tồn và giới hạn số lượng. Các dòng không còn bán hoặc thiếu hàng được báo rõ. Phụ kiện còn bán được gợi ý lại ở checkout; khách chọn lại ngày nhận, địa chỉ và mã giảm giá.

Đơn mới lưu mã lựa chọn bánh. Với đơn cũ chưa có mã, hệ thống tìm lựa chọn đang bán có nhãn trùng; không tự đổi hương vị nếu không tìm được.

## Bánh thiết kế riêng

1. Khách đăng nhập, mở Đặt bánh riêng, nhập mô tả, hương vị, số người, ngân sách, lời chúc và lịch nhận. Có thể gửi một ảnh JPG/PNG tối đa 5 MB. Đặt trước từ 3 đến 90 ngày; chọn giao hoặc nhận tại cửa hàng.
2. Admin → Bánh thiết kế riêng xem yêu cầu, báo tổng giá và mô tả rõ thiết kế/phụ kiện/phí giao đã bao gồm. Có thể từ chối với lý do khi không đáp ứng được.
3. Khách mở yêu cầu của mình và đồng ý báo giá hoặc hủy. Chỉ sau khi khách đồng ý, Admin mới chuyển sang chuẩn bị.
4. Tiến trình: Chờ báo giá → Đã báo giá → Khách đã xác nhận → Đang chuẩn bị → Sẵn sàng giao/nhận → Hoàn thành. Các thay đổi quan trọng đều có thông báo.

Yêu cầu bánh riêng có danh sách và mã TK riêng, không tạo một sản phẩm giả trong danh mục. Báo giá là tổng cuối cùng, không áp dụng voucher đơn thường. Chuyển khoản vẫn cần đối soát thủ công; đồng ý báo giá không tự đánh dấu đã thanh toán. Doanh thu dashboard cộng đơn thường và bánh riêng đã hoàn thành, phân bổ theo ngày đặt/gửi yêu cầu.

## Dữ liệu bổ sung

- `thong_bao`: thuộc một `nguoi_dung`, lưu tiêu đề/nội dung/liên kết/trạng thái đọc.
- `phu_kien`: danh mục giá và tồn phụ kiện; `phu_kien_don_hang`: ảnh chụp tên, giá, số lượng trong đơn.
- `yeu_cau_banh_rieng`: yêu cầu, ảnh mẫu, lịch nhận, trạng thái, báo giá và phản hồi; thuộc người dùng.
- `don_hang`: thêm hình thức nhận và cờ đặt trước; `chi_tiet_don_hang`: thêm mã lựa chọn để hỗ trợ mua lại.

Theo yêu cầu của chủ dự án, đợt bổ sung này chưa chạy kiểm thử hoặc chạy thử ứng dụng.
