# Bộ dữ liệu mẫu và tình huống thực hành

Bộ mẫu được tích hợp tại **phần 17** của `database/BONG_BAKERY_SQL_SERVER.sql`, sau toàn bộ cấu trúc và nâng cấp. Không có file SQL mẫu rời để tránh chạy nhầm thứ tự.

## Nạp mẫu / xóa dữ liệu cũ

1. Dừng Spring Boot để tránh ứng dụng kết nối lại khi đặt lại database.
2. Mở file SQL tổng trong SQL Server Management Studio, kết nối Windows Authentication.
3. Database chưa tồn tại: giữ `DECLARE @XoaDuLieuCu BIT=0`.
4. Database đã tồn tại và bạn muốn thay toàn bộ bằng bộ mẫu: đổi thành `DECLARE @XoaDuLieuCu BIT=1` ở đầu file. **Thao tác này xóa toàn bộ database `cake_shop`: đơn, tài khoản và dữ liệu cũ. File không tự sao lưu.** Nó không xóa các file ảnh trên ổ đĩa.
5. Chạy toàn bộ file bằng F5. Không chạy riêng phần 17 hoặc các file trong ZIP lịch sử. Các batch được chặn nếu chưa bật đặt lại hoặc một phần nâng cấp gặp lỗi được bắt.
6. Chạy lại website, đăng nhập bằng tài khoản dưới đây.

Đã chạy file tổng trên SQL Server SPQR ngày 29/10/2026, sau khi sao lưu database cũ. Các dữ liệu phát sinh trong kiểm thử được dọn để giữ lại bộ mẫu.

## 5 tài khoản

Mật khẩu chung: **123456**, lưu BCrypt trong SQL; chỉ dùng dữ liệu đồ án.

| Email | Quyền | Cách dùng |
|---|---|---|
| admin@gmail.com | ADMIN | Quản trị đơn, đối soát, voucher, QR và shipper |
| khach1@gmail.com | USER | Các đơn mẫu #1–#4, hủy, nhận quầy, đổi trả và đánh giá |
| khach2@gmail.com | USER | Đơn Stripe #5 và gửi ý tưởng bánh riêng |
| shipper1@gmail.com | SHIPPER | Phụ trách đơn đang giao #2 |
| shipper2@gmail.com | SHIPPER | Shipper dự phòng để thử đổi phân công |

Sau một lần tạo mới thành công, ID đơn thường là #1–#5. Luôn xem ghi chú `[MẪU n/5]` trong đơn để nhận đúng tình huống.

## 5 tình huống đơn hàng có sẵn

| Mẫu | Trạng thái / cách trả | Thao tác thực hành và kết quả mong đợi |
|---|---|---|
| 1 | Chờ xác nhận, COD | Khách 1 hủy: trả đúng tồn bánh, không tạo khoản hoàn khi chưa nhận tiền. Bấm lại không hoàn kho thêm. Hoặc admin xác nhận rồi thử hủy: bị từ chối. |
| 2 | Đang giao, COD, shipper 1 | Đăng nhập shipper 1 tại `/shipper`, bật GPS nếu muốn (trình duyệt cần cho phép). Khách 1 xem theo dõi. Shipper báo giao không tự cộng tiền COD. Admin chưa thể hoàn tất nếu chưa đối soát đủ tiền. Sau khi có cả bàn giao và đủ tiền mới hoàn tất. |
| 3 | Sẵn sàng nhận tại quầy, chuyển khoản, đã cọc 50% | Khách 1 mở đơn và tải QR. Admin vào `/admin/pickup`, tải ảnh QR hoặc dán mã. Khi còn nợ, không được bàn giao. Ghi nhận phần còn lại ở chi tiết đơn, rồi xác nhận qua QR; không giao lần hai. Có một nến + một thiệp, tổng đã tính riêng phụ kiện. |
| 4 | Hoàn thành, đã trả đủ, dùng BONG10 | Khách 1 chấm sao/viết đánh giá hoặc mua lại. Để thử đổi trả, gửi yêu cầu kèm 1–3 ảnh riêng trong 24 giờ kể từ mốc bàn giao mẫu (hai giờ trước lúc chạy SQL). Admin duyệt/từ chối có lý do. Nếu duyệt hoàn, ghi số đã hoàn thực tế; không được vượt mức duyệt/tiền đang giữ. Không có ảnh minh chứng giả hoặc phiếu đổi trả giả được chèn sẵn. |
| 5 | Chờ xác nhận, Stripe, chưa trả tiền | Khách 2 bấm thanh toán Stripe sau khi cấu hình Sandbox. Phải dùng phiên thực từ Stripe; SQL không tạo `cs_test`, payment intent hoặc webhook giả. Quay về trang thành công không tự ghi tiền. Cần webhook hợp lệ, sau đó admin mới xác nhận/làm bánh. |

Lịch mẫu được tính từ ngày chạy SQL. Nếu để dữ liệu lâu, đơn mới có thể quá lịch và đơn hoàn thành hết hạn đổi trả; tạo đơn mới hoặc đặt lại bộ mẫu khi muốn thực hành lại.

## Bánh thiết kế riêng

- Khách 2: một yêu cầu giao tận nơi, chờ báo giá, lịch sau 7 ngày. Dùng để thử báo giá → chốt → gửi mẫu → khách duyệt → đối soát → làm bánh.
- Khách 1: một yêu cầu nhận quầy, đã chốt và duyệt ảnh mẫu, đã cọc 325.000/650.000 đ; lịch sau 5 ngày. Admin có thể bắt đầu chuẩn bị → sẵn sàng. Khách có QR; thu đủ phần còn lại mới bàn giao. Ảnh tham khảo dùng ảnh bánh đã có trong project.

## Voucher mới

Các mã đang dùng hết hạn sau 90 ngày kể từ khi nạp mẫu. Một mã/đơn, chỉ giảm tiền bánh; không giảm phụ kiện hoặc phí giao và không cộng gộp.

| Mã | Giảm | Tiền bánh tối thiểu | Lượt chung / mỗi người |
|---|---|---:|---:|
| BONG10 | 10%, tối đa 50.000 đ | 100.000 đ | 100 / 1 |
| NGOT50K | 50.000 đ | 300.000 đ | 100 / 2 |
| SINHNHAT15 | 15%, tối đa 120.000 đ | 500.000 đ | 50 / 1 |
| BANH20K | 20.000 đ | 150.000 đ | 100 / 3 |
| BANMOI30K | 30.000 đ | 250.000 đ | 1 / 1 |
| HETHAN10 | Cố tình hết hạn | 0 đ | Luôn bị từ chối vì hết hạn |

BONG10 đã được Khách 1 dùng đúng một lần trên đơn mẫu #4, có lịch sử sử dụng tương ứng. Khách 1 thử lại sẽ bị giới hạn lượt; Khách 2 vẫn còn lượt. Dùng BANMOI30K một lần để thử hết lượt chung; hủy đơn vẫn không hoàn lượt. Thử NGOT50K với tiền bánh dưới 300.000 đ để kiểm tra điều kiện tối thiểu. WELCOME10 và SWEET30 cũ vẫn được giữ trong bộ cài mới.

## Tồn kho, thông báo và tiền mẫu

SQL trừ đúng tồn bánh/phụ kiện cho các đơn và ghi sổ kho, tiền cọc/thu có lịch sử đối soát. Có thêm bánh tồn 2 và bánh hết hàng để thử dashboard; admin có thông báo liên kết tới bộ lọc tồn kho. Không tạo GPS shipper giả. Tất cả số tiền và ghi chú `DEMO` là dữ liệu thực hành, không phải tiền thật.

## QR có tự tạo không?

Có. Website tạo PNG tại máy chủ bằng ZXing khi khách mở chi tiết đơn nhận tại cửa hàng; không cần API tạo QR. QR gắn với mã riêng của đơn, không phải QR thanh toán ngân hàng. Khách chỉ xem/tải QR của mình; admin tra trong **Nhận bánh bằng QR**. Trạng thái và số tiền được đọc lại từ database khi bàn giao, không tin nội dung QR để xác nhận đã trả tiền.

Phần 18 bổ sung dữ liệu đối soát hoàn thực tế. Phần 19 bổ sung giỏ tài khoản, địa chỉ mặc định và lựa chọn nhận gần nhất. Khách 1/2 có một bánh trong giỏ mẫu; Khách 1 được gợi ý nhận tại quầy 13:00–17:00, Khách 2 giao tận nơi cùng khung giờ.
