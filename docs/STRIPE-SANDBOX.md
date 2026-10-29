# Stripe Sandbox trên localhost — Bóng Bakery

## Đã tích hợp

- Thanh toán thẻ qua trang Stripe Checkout cho đơn giỏ hàng và bánh thiết kế riêng sau khi khách đồng ý báo giá.
- Giao tận nơi: trả đủ trước bằng Stripe. Nhận tại cửa hàng: chọn cọc 50% hoặc trả đủ; lần thanh toán tiếp theo thu phần còn lại khi đủ điều kiện.
- Trang kết quả và lịch sử từng lần thanh toán. Chỉ chủ đơn được tạo/tiếp tục/đóng phiên; chủ đơn và admin được xem kết quả.
- Webhook xác minh chữ ký, chỉ nhận môi trường thử; kiểm tra phiên, mã lần thanh toán, tiền tệ, số tiền và mã giao dịch. Ghi nhận một lần dù Stripe gửi lại thông báo.
- Admin có menu **Stripe Sandbox** để xem giao dịch và xử lý trường hợp cần đối soát. Không tự mở lại đơn đã hủy, không tự thu thêm/hoàn tiền khi lưu kết quả đối soát.

Đây là **sandbox**, không thu tiền thật. Tích hợp hiện từ chối khóa và phiên live.


## Tải từ GitHub hoặc chuyển sang máy khác

Chức năng Stripe, thư viện Maven và các script nằm trong mã nguồn. `.local/` chứa khóa riêng và CLI nên được loại khỏi Git; không bỏ quy tắc này và không đưa khóa vào SQL.

1. Import project Maven vào Eclipse, chọn **Maven → Update Project**.
2. Mở `THIET_LAP_STRIPE.cmd` ở thư mục gốc dự án. Hoặc chạy `powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\Thiet-lap-Stripe-Sandbox.ps1`.
3. Khi được hỏi, nhập **secret key Sandbox của bạn** trực tiếp trong cửa sổ thiết lập; ký tự được ẩn. Không gửi khóa trong chat và không commit khóa. Chỉ nhận `sk_test_`/`rk_test_`, không nhận khóa live.
4. Script tạo `.local/stripe.properties` từ `config/stripe.properties.example`, tải Stripe CLI từ bản phát hành chính thức v1.55.3 và đối chiếu SHA-256 nếu máy chưa có CLI. CLI đặt riêng trong project, không cần cài toàn máy hay sửa PATH.
5. Script bật chuyển tiếp webhook và lưu khóa ký vào file riêng. Sau khi thông báo kết nối thành công, khởi động Spring Boot từ thư mục gốc project.
6. Sau mỗi lần khởi động lại máy, chạy `tools/Bat-Stripe-Sandbox.ps1` rồi chạy website. Nếu đổi khóa hoặc địa chỉ chạy, chạy lại kết nối và khởi động lại ứng dụng.

Stripe Checkout dạng chuyển trang này không cần `pk_test` ở giao diện. Khóa secret chỉ ở máy chủ. File mẫu không chứa khóa thật và không tự bật thanh toán khi chưa cấu hình. Bạn cũng có thể dùng các biến môi trường `STRIPE_ENABLED`, `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, `STRIPE_BASE_URL` khi không dùng file riêng.

Dữ liệu mẫu có đơn Stripe #5 đang chờ thanh toán để thực hành. Không có phiên hoặc khoản thu Stripe giả trong SQL. QR nhận bánh tại quầy là tính năng riêng, hoạt động không cần Stripe.

Tham khảo cách cài từ [tài liệu Stripe CLI chính thức](https://docs.stripe.com/cli/install) và [bản phát hành được script sử dụng](https://github.com/stripe/stripe-cli/releases/tag/v1.55.3).

## Chạy trên máy hiện tại

Khóa thử đã được lưu riêng trong `.local/stripe.properties`, thư mục này được loại khỏi Git. Không sao chép file này vào tài liệu, SQL hoặc ảnh chụp. Stripe CLI chính thức được lưu tại `.local/tools/stripe.exe`, không thay PATH hệ thống. Có đủ SDK để chạy Stripe Checkout; khách nhập thẻ trên trang Stripe, website không nhận/lưu số thẻ hoặc CVC.

1. Trong Eclipse: Refresh dự án (F5), **Maven → Update Project** để nhận thư viện `stripe-java`.
2. Chạy kết nối webhook bằng PowerShell tại thư mục dự án:

   ```powershell
   .\tools\Bat-Stripe-Sandbox.ps1
   ```

   Script dùng khóa thử trong cấu hình riêng, chạy CLI ẩn, chuyển tiếp các sự kiện Checkout đến `app.stripe.base-url` + `/payments/stripe/webhook` (mặc định localhost:8080), tự lưu khóa ký webhook vào cấu hình. Không đưa khóa API vào tham số dòng lệnh và không in khóa ra màn hình. Chỉ tạo kết nối, không tạo giao dịch. Trạng thái kết nối phụ thuộc tiến trình CLI trên máy đang chạy.

3. Khởi động lại **Spring Boot App** ở cổng 8080. Cấu hình riêng được đọc khi khởi động; cần chạy từ thư mục dự án như cấu hình `Chay-website-SQL-Server.launch`.
4. Chọn Stripe Sandbox khi đặt hàng, tạo đơn rồi bấm **Thanh toán Stripe Sandbox** trong chi tiết đơn. Bánh riêng cần khách xác nhận báo giá trước khi thanh toán.
5. Stripe chuyển về trang kết quả. Giữ máy, mạng và kết nối webhook hoạt động để ghi nhận; không cần ngrok.

Sau khi khởi động lại máy cần chạy lại script bật webhook rồi khởi động ứng dụng. Script chỉ tái sử dụng đúng tiến trình CLI của dự án; khi đổi khóa sẽ khởi động lại kết nối. Nếu thay tài khoản/khóa thử, cập nhật cấu hình riêng rồi chạy lại script. Khóa ký của Stripe CLI có thể khác khóa ký của webhook tạo trên Dashboard, không dùng lẫn.

Dừng riêng kết nối webhook:

```powershell
.\tools\Tat-Stripe-Sandbox.ps1
```

Nếu webhook tắt, người dùng có thể đã trả tiền trên trang thử Stripe nhưng đơn chưa cập nhật. Chạy lại kết nối và kiểm tra/đối soát giao dịch trước khi yêu cầu khách thanh toán lại.

## Thẻ thử

Chỉ nhập thẻ thử trên trang Stripe Checkout:

- Số thẻ thành công thông dụng: `4242 4242 4242 4242`.
- Ngày hết hạn ở tương lai, ví dụ `12/34`.
- CVC thử: `123`; các thông tin khác dùng dữ liệu thử.

Không dùng thẻ thật trong sandbox. Xem [thẻ và tình huống kiểm thử Stripe](https://docs.stripe.com/testing).

## Cách xác nhận tiền

Giá và khoản cần thu lấy từ đơn lưu trong database, không nhận số tiền do trình duyệt gửi lên. VND là đơn vị không có phần thập phân: số nguyên VND được gửi trực tiếp làm `unit_amount`, không nhân thêm 100. Nếu tài khoản Stripe không hỗ trợ khoản tiền/VND đó hoặc số tiền dưới mức Stripe cho phép, việc tạo phiên bị từ chối và không ghi nhận tiền.

Mã chống trùng được lưu trước khi gọi Stripe; gửi lại dùng cùng mã và tham số. Mỗi đơn chỉ có một phiên đang mở theo luồng ứng dụng. Đóng phiên xác minh với Stripe trước khi ghi hết hạn. Quay về từ Checkout, tự sửa query `returned=true` hoặc bấm Làm mới **không** làm tăng số tiền đã trả. Chỉ thông báo Stripe đã xác minh mới ghi nhận khoản thu; không tự chuyển trạng thái làm bánh.

Nếu khách trả tiền sau khi đã hủy đơn: giữ trạng thái hủy/hoàn kho, ghi nhận khoản thu thử phù hợp rồi đưa vào đối soát. Nếu thông báo sai số tiền/tiền tệ hoặc vượt số còn phải trả, lưu trạng thái cần kiểm tra, không tự cộng vào đơn. Khi có giao dịch cần đối soát, chặn tạo thêm phiên đến khi admin xử lý.

Admin kiểm tra trên Stripe Sandbox, dùng chức năng đối soát thu/hoàn hiện có của cửa hàng khi phù hợp rồi ghi kết quả tại **Stripe Sandbox**. Nút lưu đối soát chỉ ghi xác nhận thủ công, không gọi API hoàn tiền. Việc hoàn trên Stripe chưa được tự động tích hợp trong lần này; thực hiện và xác minh trên Dashboard thử trước khi ghi sổ, không ghi nhận một khoản thu/hoàn hai lần.

Hết hạn/đóng phiên Stripe không tự hủy đơn hoặc hoàn kho; khách hủy đơn theo quy tắc hiện có. Không tự đổi phương thức thanh toán cho các đơn cũ COD/chuyển khoản. Các phiên sandbox và khoản thu có nhãn rõ ràng, không sử dụng dữ liệu thử như số liệu thu tiền thực tế.

## Cấu hình và dữ liệu

Các thuộc tính: `app.stripe.enabled`, `app.stripe.secret-key`, `app.stripe.webhook-secret`, `app.stripe.base-url`. Cấu hình mặc định tắt, file `.local/stripe.properties` trên máy này đã được cấu hình để ghi đè mặc định. Không đóng gói thư mục `.local` hoặc chia sẻ khóa. Nếu khóa đã xuất hiện trong chat, nên thu hồi và thay bằng khóa thử mới sau khi thiết lập; nhập khóa mới trực tiếp tại file riêng, không cần gửi vào chat.

Đã bổ sung bảng `giao_dich_stripe` và cho phép phương thức `STRIPE` trên SQL Server SPQR, không thay đơn hoặc số tiền cũ. File tổng `database/BONG_BAKERY_SQL_SERVER.sql` đã tích hợp **phần 15**; tổng cấu trúc hiện có 25 bảng. Cấu trúc trên máy hiện tại không cần chạy lại. File tổng hiện có thêm phần 16 (tọa độ bánh riêng), phần 17 (bộ mẫu) và tùy chọn xóa database ở đầu file. Muốn nạp bộ mẫu lên database đã tồn tại phải chủ động bật đặt lại, toàn bộ dữ liệu cũ sẽ bị xóa; không chạy riêng các phần của file tổng.

## Trạng thái bàn giao

Đã biên dịch mã và thiết lập kết nối webhook bằng khóa thử được cung cấp. **Chưa chạy bộ kiểm thử mới hoặc giao dịch Stripe từ đầu đến cuối trong lần này**, theo yêu cầu chỉ kiểm thử khi được yêu cầu. Kết quả 111 kiểm thử trước đó thuộc phiên bản trước khi thêm Stripe.

Tài liệu chính thức: [Stripe Java](https://github.com/stripe/stripe-java), [Checkout Session](https://docs.stripe.com/api/checkout/sessions/create?lang=java), [webhook và localhost](https://docs.stripe.com/webhooks), [Stripe CLI listen](https://docs.stripe.com/cli/listen).
