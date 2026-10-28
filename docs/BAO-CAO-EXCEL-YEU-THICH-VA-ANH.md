# Các chức năng bổ sung

> Cập nhật 28/10/2026: `database/` chỉ dùng một file chính thức `BONG_BAKERY_SQL_SERVER.sql` cho cài mới. Các file nâng cấp riêng được giữ trong `database/LUU_TRU_SQL_CU_2026-10-28.zip`; các số file bên dưới là lịch sử. Database SPQR đã nâng cấp không cần chạy lại.

## Báo cáo theo thời gian

Trong admin, mở **Báo cáo theo thời gian** (`/admin/reports`). Chọn ngày bắt đầu/kết thúc (tối đa 366 ngày), ngày tạo hoặc ngày hoàn thành, và loại đơn. Nút xuất PDF dùng đúng bộ lọc đang chọn, có bảng ngày và chi tiết đơn.

Doanh thu = tổng giá trị đơn đã hoàn thành trong tập đơn được lọc − số tiền đã ghi nhận hoàn của các đơn đó. Giá trị đơn trung bình = doanh thu / số đơn hoàn thành. Báo cáo có tính đơn bánh riêng. Đây là báo cáo giá trị đơn, không phải dòng tiền thu theo ngày; tiền hoàn là tổng hiện tại của tập đơn, không phải riêng số tiền hoàn phát sinh trong khoảng ngày. Đơn cũ chưa có thời điểm hoàn thành có thể xem bằng bộ lọc ngày tạo.

## Nhập Excel

Mở **Nhập bằng Excel** (`/admin/import`), tải đúng mẫu rồi điền ở sheet `Du lieu`. Có mẫu thêm bánh, thêm nến/thiệp, điều chỉnh tồn kho. File `.xlsx` tối đa 2 MB, 300 dòng; không dùng công thức/macro. Giá và số lượng là số nguyên; trạng thái 1/0. Danh mục phải tồn tại trước khi thêm bánh.

Tải lên để xem trước, sửa hết lỗi rồi bấm xác nhận. Chưa xác nhận thì chưa ghi dữ liệu. Bản xem trước có hiệu lực 30 phút và chỉ thuộc tài khoản tạo. Nếu một dòng không còn hợp lệ khi xác nhận, toàn bộ lần nhập được hủy. Mẫu kho dùng số lượng cộng/trừ và lý do, không ghi đè tồn tuyệt đối. Ảnh/video bổ sung trong trang sửa bánh.

## Yêu thích và xóa phụ kiện

Nút tim xuất hiện trên thẻ bánh và trang chi tiết. Khách đăng nhập có danh sách riêng tại `/favorites`; lưu yêu thích không giữ tồn kho. Phụ kiện có nút xóa kèm xác nhận. Phụ kiện đã có trong đơn chỉ ngừng bán để giữ lịch sử, phụ kiện chưa được sử dụng được xóa.

## Quên mật khẩu

Đã có trang yêu cầu và đặt lại mật khẩu, liên kết dùng một lần trong 15 phút. Đặt lại thành công sẽ làm phiên đăng nhập cũ hết hiệu lực. Theo lựa chọn của chủ dự án, gửi email mặc định **tắt**, giao diện báo rõ chưa sẵn sàng; chưa thể gửi thư thật trước khi cấu hình SMTP.

Khi cấu hình trong Environment của Eclipse, đặt `MAIL_ENABLED=true`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `APP_PUBLIC_BASE_URL`. Mặc định cổng 587, STARTTLS và xác thực bật; điều chỉnh `MAIL_STARTTLS`, `MAIL_SMTP_AUTH` theo nhà cung cấp. Địa chỉ công khai phải trỏ đúng website chạy thực tế. Không đưa thông tin bí mật vào mã nguồn hoặc chat.

## Danh mục, ảnh và mô tả

Nhấn **Bộ sưu tập bánh** mở thẳng `/products`. Có 13 danh mục mẫu, mỗi danh mục 3 bánh (39 bánh), không có bánh trung thu. Bộ lọc danh mục nằm trong trang bộ sưu tập.

Ảnh tải từ Internet, không dùng ảnh tạo bằng AI. Tất cả 39 ảnh đặt tại `src/main/resources/static/images/banh/`; `SOURCES.json` ghi nguồn, tác giả, giấy phép, mã kiểm tra và mô tả tiếng Việt; `NGUON-ANH.md` là bảng nguồn dễ đọc. Trang `/image-credits` và liên kết dưới ảnh chi tiết công khai thông tin tác giả/giấy phép. Không chỉnh sửa file ảnh tải về; giao diện có thể cắt khung hiển thị bằng CSS. Đây là ảnh minh họa kiểu bánh; trang trí và quy cách thực tế cần cửa hàng xác nhận.

Đã cập nhật ảnh và mô tả 39 bánh mẫu trong SQL Server SPQR. Không thay giá, tồn, dị ứng, thành phần hay ảnh admin upload. Admin vẫn có thể thay ảnh thật bằng chức năng upload; khi thay, ghi nguồn của ảnh mẫu không còn xuất hiện dưới ảnh đó.

Máy khác có database cũ: chạy lần lượt `10-yeu-thich-va-quen-mat-khau.sql`, `11-moi-danh-muc-ba-banh.sql`, `12-anh-va-mo-ta-banh.sql` sau các bản nâng cấp trước đó. Database mới dùng `BONG_BAKERY_SQL_SERVER.sql` đã tích hợp các phần trên. Không chạy file tạo đầy đủ lên database đang có dữ liệu.
