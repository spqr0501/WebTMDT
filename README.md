# Tiệm bánh ngọt Bóng Bakery

## Bổ sung mới

Báo cáo theo thời gian và PDF, nhập Excel có xem trước, danh sách yêu thích, xóa nến/thiệp và quên mật khẩu (SMTP cấu hình sau). Bộ sưu tập có 13 danh mục × 3 bánh, 39 ảnh lấy trên mạng kèm nguồn, mô tả tiếng Việt đầy đủ.

Xem [hướng dẫn sử dụng và cấu hình](docs/BAO-CAO-EXCEL-YEU-THICH-VA-ANH.md). Ảnh nằm trong `src/main/resources/static/images/banh/`; nguồn hiển thị tại `/image-credits`.

## Gợi ý tìm kiếm khi đang gõ

- Ô tìm kiếm ở đầu trang và trang bộ sưu tập tự hiện tối đa 6 bánh có tên chứa từ khóa. Mỗi dòng gồm ảnh quản trị đã tải lên, tên bánh, giá và mô tả ngắn; bánh hết hàng được ghi rõ.
- Bấm kết quả để mở chi tiết; nhấn Enter để tìm tất cả. Có thể dùng phím ↑/↓ để chọn kết quả và Enter để mở, Escape để đóng.
- Gợi ý đợi 250 mili giây sau khi gõ, hủy yêu cầu cũ khi thay từ khóa và hỗ trợ quá trình nhập tiếng Việt. Khi không tải được gợi ý, nút tìm kiếm thông thường vẫn dùng được.
- Tên hiển thị cửa hàng đã đổi thành **Bóng Bakery**. Tài khoản đăng nhập đang có tiếp tục sử dụng như trước.
- Theo yêu cầu của chủ dự án, chỉ chạy kiểm thử khi được yêu cầu rõ ràng.

Dự án hiện dùng **Microsoft SQL Server**, đăng nhập bằng tài khoản Windows trên máy **SPQR**, cùng Spring Boot 3.5.7, Java 17+, Thymeleaf, Spring Security và Maven.

## Chạy website trong Eclipse

Xem [hướng dẫn chạy SQL Server bằng tài khoản Windows](docs/CHAY-SQL-SERVER.md).

1. Nhấn F5 tại dự án, rồi **Maven → Update Project**.
2. Chạy Maven với **Goals = initialize**, **Profiles để trống** để chuẩn bị thư viện xác thực.
3. Chạy file **Chay-website-SQL-Server.launch**, hoặc Maven với **Goals = spring-boot:run**.
4. Đợi `Started WebTmdtApplication`, mở **http://localhost:8080**.

`pom.xml` không phải tên profile. Các lệnh `test` / `verify` chạy kiểm thử, không giữ website hoạt động. Kết nối mặc định: SPQR:1433, database cake_shop, Windows Authentication. Không điền mật khẩu SQL Server.

Để tạo quản trị viên lần đầu, đặt `ADMIN_EMAIL` và `ADMIN_PASSWORD` (ít nhất 12 ký tự, tối đa 72 byte UTF-8) trong Environment của cấu hình chạy Eclipse. Nếu email đã tồn tại, ứng dụng không đổi quyền/mật khẩu của tài khoản đó.

## Cơ sở dữ liệu và tài liệu

- [File SQL Server đầy đủ, tên bảng/cột tiếng Việt](database/01-tao-day-du-sql-server.sql): các bảng, khóa và chỉ mục, 13 danh mục và 39 bánh mẫu có ảnh và mô tả; tích hợp nâng cấp đến file 12. Chỉ chạy khi chưa có các bảng.
- [Thiết kế dữ liệu và kế hoạch](docs/THIET-KE.md).
- [Quy tắc mã giảm giá](docs/VOUCHER.md).
- [Kiểm thử](docs/KIEM-THU.md).

Các bảng: `nguoi_dung`, `danh_muc`, `san_pham`, `dia_chi`, `don_hang`, `chi_tiet_don_hang`, `ma_giam_gia`, `luot_su_dung_ma`. Các trường tiếng Việt dùng Unicode; `ddl-auto=validate` kiểm tra cấu trúc, không xóa hay tự sửa database.

Các file `01-schema.sql`, `02-seed.sql`, `03-doi-ten-tu-schema-cu.sql` là bản MySQL cũ để tham khảo, **không chạy chúng trên SQL Server**.

## Lệnh thường dùng

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd verify
```

Muốn chạy bản đóng gói với Windows Authentication:

```powershell
java "-Djava.library.path=./native" -jar target/WebTMDT-1.0.0.jar
```

Cần JDK 64-bit và thư mục `native` cạnh nơi chạy lệnh. Maven tự tải thư viện Microsoft đúng phiên bản; không cần thay đổi PATH hệ thống.

## Chức năng đã triển khai

**Khách hàng:** trang chủ/banner, danh mục, sản phẩm nổi bật, tìm kiếm và lọc theo tên/danh mục/khoảng giá, sắp xếp và phân trang, chi tiết bánh/ảnh/video/tồn kho, giỏ hàng session, đăng ký/đăng nhập/đăng xuất, lưu/chọn địa chỉ, xem trước mã giảm giá, đặt hàng COD/chuyển khoản, lịch sử/chi tiết đơn và hủy đơn chờ xác nhận.

**Quản trị:** bảng tổng quan, CRUD danh mục, tạo/sửa/ẩn sản phẩm, tải lên ảnh/video, điều chỉnh kho có khóa bản ghi, xem đơn và chuyển trạng thái đúng bước, danh sách/khóa/mở khóa người dùng, tạo/sửa/xóa hoặc vô hiệu hóa voucher. Sản phẩm đã bán được ẩn thay vì xóa vật lý; mã giảm giá đã dùng được vô hiệu hóa để giữ lịch sử.

**Dữ liệu:** lưu giá, tên sản phẩm, thông tin giao hàng, phí giao hàng và giảm giá tại thời điểm đặt đơn; giao dịch đặt hàng toàn vẹn; khóa tồn kho/voucher; token chống gửi đơn lặp; giới hạn một mã giảm giá mỗi đơn.

## Cấu trúc

```text
src/main/java/com/example/demo/
  config/       Security, kiểm tra tài khoản đang hoạt động, tạo admin, múi giờ
  controller/   Controller khách hàng/admin, media, xử lý lỗi chung
  dto/          Form DTO với Bean Validation, không bind trực tiếp Entity
  entity/       16 Entity và enum
  repository/   15 Spring Data Repository, truy vấn và khóa bản ghi
  service/      Catalog, giỏ hàng, đặt hàng, voucher, tài khoản, media, admin
src/main/resources/
  templates/fragments/shop.html    Header/footer dùng chung phía khách hàng
  templates/fragments/admin.html   Header/sidebar/footer dùng chung AdminLTE
  templates/shop|auth|account|admin/
  static/css/                     CSS cửa hàng
  static/images/                  Hình minh họa SVG mẫu, không cần ảnh mạng
database/                         Cấu trúc và dữ liệu mẫu SQL Server
docs/                             Thiết kế, voucher, nghiệm thu
src/test/                         Kiểm thử tích hợp trên H2 ở chế độ SQL Server
```

Muốn sửa header/footer: chỉ sửa fragment tương ứng; các trang dùng `th:replace` tự nhận thay đổi. Không nạp Bootstrap 5 vào trang AdminLTE 3: AdminLTE 3 yêu cầu Bootstrap 4 và jQuery, đã được tách riêng.

## Kiểm thử

```powershell
.\mvnw.cmd test
```

Test tự dùng profile `test` và cơ sở dữ liệu H2 trong bộ nhớ; không kết nối hoặc sửa cơ sở dữ liệu SQL Server của bạn. Bao gồm render các template qua MockMvc, phân quyền/CSRF, đăng nhập thực, giá và mã giảm giá, hoàn tác giao dịch, quyền sở hữu đơn/địa chỉ, hủy đơn, chống lặp đơn và cạnh tranh mua sản phẩm cuối cùng. Kết quả ở `target/surefire-reports`.

H2 không thay thế kiểm thử tích hợp SQL Server thật. Trước nghiệm thu, dùng file SQL Server đầy đủ và chạy danh sách kiểm tra SQL Server trong `docs/KIEM-THU.md`.

## Lưu ý vận hành MVP

- Chuyển khoản là đối soát thủ công. Chưa tích hợp cổng thanh toán, webhook, email hay API vận chuyển. Không tự đánh dấu khách đã trả tiền. Quản trị chỉ chuyển đơn sang chuẩn bị sau khi xác nhận thông tin/thanh toán.
- Giỏ hàng lưu trong session, chưa đồng bộ đa thiết bị. Hàng chỉ trừ khi đặt đơn thành công, chưa giữ khi thêm giỏ. Không tự hết hạn đơn chờ: quản trị viên xử lý hoặc hủy trước khi chuẩn bị.
- Mã giảm giá không hoàn lượt khi hủy; quy tắc này hiển thị tại checkout. Đơn đã chuyển khoản nhưng hủy cần liên hệ cửa hàng để xử lý hoàn tiền thủ công.
- Tải lên ảnh tối đa 5 MB, MP4 tối đa 30 MB, kiểm tra chữ ký tệp, tên UUID, không nhận HTML/SVG từ người dùng. Cần sao lưu thư mục `uploads` cùng database. Video mẫu không kèm sẵn; quản trị viên tải MP4 ở màn hình sửa sản phẩm.
- Hình bánh mẫu là minh họa SVG có sẵn trong source; thay bằng ảnh thật trước khi sử dụng kinh doanh.
- Các thư viện giao diện tải từ CDN. Khi cần chạy offline, tải đúng phiên bản vào `static/vendor` và sửa đường dẫn trong hai fragment. Tải lên cũ chưa có tác vụ dọn tự động.
- Múi giờ nghiệp vụ: `Asia/Ho_Chi_Minh`. Tiền VND dùng `BigDecimal`, không dùng số thực nhị phân.
- Khi đưa lên Internet cần cấu hình HTTPS, cookie Secure, quản lý secrets, sao lưu, rate limit đăng nhập và rà soát bảo mật; bản này phục vụ MVP/đồ án.

## Nguồn tương thích công nghệ

- [Spring Boot 3.5 system requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [AdminLTE 3.2 documentation](https://adminlte.io/docs/3.2/)


## Nâng cấp trang chi tiết và đánh giá (25/10/2026)

- Database đã có dữ liệu: chạy `database/04-nang-cap-trang-va-danh-gia.sql` một lần trong SSMS, sau đó khởi động lại Spring Boot. Script có thể chạy lại, không xóa dữ liệu cũ. Bản `01-tao-day-du-sql-server.sql` đã bao gồm cấu trúc mới cho cài đặt từ đầu.
- Trong Eclipse: Refresh dự án (F5), dừng phiên chạy cũ rồi Run As → Spring Boot App. Giữ cấu hình thư viện Windows Authentication trong `native` như phần hướng dẫn trên.
- Trang chủ có bánh mới, bánh bán chạy dựa trên đơn hoàn thành, mã giảm giá còn hiệu lực và liên kết thông tin. Không tạo đánh giá giả hoặc số liệu bán hàng giả.
- Header, footer và thanh tìm kiếm dùng chung tại `templates/fragments/shop.html`; phần quản trị dùng fragment riêng.
- Admin → Sản phẩm & tồn kho → **Sửa**: tải ảnh bìa/video, nhập thành phần, thông tin dị ứng và bảo quản. **Ảnh & lựa chọn**: tải thêm tối đa 6 ảnh, gỡ ảnh, thêm hoặc ngừng bán kích thước/hương vị. Các lựa chọn dùng chung tồn kho; giá bán bằng giá gốc cộng giá thêm. Đơn hàng lưu lại tên lựa chọn và giá lúc mua.
- Checkout: ngày nhận từ ngày mai đến 30 ngày tới, ba khung giờ dự kiến và lời chúc tối đa 100 ký tự. Cửa hàng kiểm tra khả năng giao khi xác nhận đơn. Không tự xác nhận lịch với đơn vị vận chuyển.
- Đánh giá: chỉ tài khoản có đơn chứa bánh đó ở trạng thái **Hoàn thành**; mỗi tài khoản một đánh giá/sản phẩm. 1–5 sao, nội dung 10–2.000 ký tự, tối đa 3 ảnh JPG/PNG (5 MB/ảnh, tối đa 16 triệu điểm ảnh). Ảnh được giải mã và ghi lại PNG để loại dữ liệu đính kèm. Đánh giá hiển thị ngay sau khi gửi thành công.
- Admin → **Đánh giá khách hàng**: ẩn với lý do hoặc hiện lại. Điểm trung bình, biểu đồ sao và bộ lọc chỉ tính đánh giá đang hiển thị. Ẩn đánh giá không xóa tệp gốc; liên kết tệp đã biết vẫn có thể mở.
- Admin → **Lịch sử tồn kho**: ghi nhận kho ban đầu của sản phẩm mới, điều chỉnh thủ công, đặt hàng và hoàn kho do hủy. Các biến động trước nâng cấp không được tạo lại giả định.
- Dashboard: lọc tháng, doanh thu các đơn đã hoàn thành theo **ngày đặt đơn**, gồm phí giao hàng. Đơn hàng có bộ lọc trạng thái.
- Thông tin liên hệ lấy từ `SHOP_PHONE`, `SHOP_ADDRESS`, `SHOP_HOURS` trong Environment của cấu hình chạy Eclipse (hoặc chỉnh `app.shop.*` trong `application.properties`). Khi chưa nhập, trang liên hệ hiển thị “Đang cập nhật”.
- Hình minh họa hiện có được giữ lại; quản trị viên tự tải ảnh thật sau. Bộ ảnh và video dùng tệp cục bộ trong thư mục `uploads`; gỡ ảnh khỏi sản phẩm không xóa tệp vật lý nhằm tránh mất ảnh đang được tham chiếu.

+
+## Thông báo, nhận tại cửa hàng và bánh riêng
+
+Đã bổ sung thông báo cho khách/Admin, bước xác nhận đơn, nhận tại cửa hàng, mua lại đơn cũ, mua bánh kèm nến/thiệp và yêu cầu thiết kế bánh có báo giá. Đơn đặt trước hỗ trợ đến 90 ngày; bánh riêng đặt trước 3–90 ngày.
+
+Xem thao tác và quy tắc tại [Hướng dẫn chức năng đặt bánh mới](docs/CHUC-NANG-DAT-BANH-MOI.md). Database đang dùng nâng cấp bằng `database/05-thong-bao-combo-va-banh-rieng.sql`; cài mới dùng file 01 đã bao gồm phần bổ sung. Phụ kiện mẫu có tồn bằng 0, nhập tồn thực tế tại Admin → Nến & thiệp.
+
+Theo yêu cầu của chủ dự án, chưa chạy kiểm thử hoặc chạy thử ứng dụng trong đợt bổ sung này.
+
+
+## Giao diện hồng Bóng Bakery
+
+- Cửa hàng sử dụng nền hồng pastel và họa tiết bong bóng SVG cục bộ trong `static/images/pink-bubbles.svg`; không cần tải ảnh nền từ mạng. Các thẻ trắng giữ nội dung dễ đọc.
+- Phần trình bày cửa hàng nằm trong `static/css/pink-shop.css`, được nạp sau `shop.css`. Giao diện Admin nằm trong `static/css/pink-admin.css`, tiếp tục dùng AdminLTE 3 và Bootstrap 4 riêng biệt với phía khách.
+- Thanh ưu đãi đầu trang có nút ×. Lựa chọn đóng được giữ trong `sessionStorage` của tab hiện tại khi chuyển trang hoặc tải lại; tab/phiên mới sẽ hiển thị lại. Nếu trình duyệt chặn lưu trữ, nút vẫn ẩn được trên trang đang mở.
+- Dashboard dùng số liệu thật đang có, bao gồm trạng thái chưa có doanh thu/đơn hàng. Menu Admin đánh dấu mục hiện tại; bảng, trạng thái đơn và biểu mẫu dùng cùng hệ màu.
+- Sau khi cập nhật, Refresh dự án trong Eclipse, khởi động lại Spring Boot App và Ctrl + F5 trên trình duyệt. Đợt đổi giao diện này chưa chạy kiểm thử hoặc chạy thử theo yêu cầu của chủ dự án.
+
+
+## Hóa đơn PDF và cọc khi nhận tại cửa hàng
+
+Nhận tại cửa hàng có hai lựa chọn: cọc 50% hoặc trả đủ trước. Admin đối soát tiền trước khi xác nhận/chuẩn bị; số còn lại phải được ghi nhận trước khi hoàn thành đơn pickup. Đơn thường và bánh riêng có trang hóa đơn cùng nút tải PDF theo quyền khách/Admin.
+
+Xem [Hướng dẫn hóa đơn và tiền cọc](docs/HOA-DON-VA-TIEN-COC.md). Schema nâng cấp ở file 07; Eclipse cần Maven → Update Project để nhận PDFBox. Phông tiếng Việt Noto Sans và giấy phép kèm sẵn. Dòng chuyển khoản trong cấu hình đã sửa cách mã hóa.
+

## Bổ sung ngày 25/10: duyệt mẫu, QR nhận bánh và lịch sản xuất

- Database đã có dữ liệu: chạy `database/08-duyet-mau-va-lich-san-xuat.sql` sau file 07. Không chạy lại file 01 để nâng cấp. File 01 đã gồm file 08 cho cài mới.
- Eclipse: Maven → Update Project để tải thư viện ZXing; khởi động lại bằng Run As → Spring Boot App.
- Thông tin dị ứng và bảo quản: quản trị → Bánh & tồn kho → Sửa; khách xem tại chi tiết bánh. Dữ liệu trống hiển thị lời nhắc xác nhận với cửa hàng.
- Duyệt mẫu: báo giá bánh riêng → admin gửi ảnh mẫu → khách duyệt mẫu mới nhất và chốt báo giá → đủ tiền trả trước → bắt đầu làm. Đơn đang ACCEPTED từ trước cũng cần duyệt mẫu; đơn đã làm tiếp tục trạng thái hiện tại.
- QR nằm trong chi tiết đơn nhận tại cửa hàng; admin mở `/admin/pickup`, tải ảnh QR hoặc dán mã; kiểm tra tên khách, tiền và trạng thái rồi xác nhận bàn giao. Không cần mở camera; QR không dùng để thanh toán.
- Lịch bếp: `/admin/production`, lọc ngày và trạng thái, tổng bánh theo loại/biến thể, phụ kiện và lời chúc. Chưa giới hạn công suất mỗi giờ.
- Dashboard và danh sách bánh có nút lọc sắp hết (1–4), đã hết (0); thông báo kho đi thẳng tới bộ lọc. Chỉ tính bánh đang bán.
- Xem `docs/DOI-CHIEU-5-YEU-CAU.md` để biết yêu cầu đã có, còn thiếu và quy tắc tính dashboard. Đợt bổ sung này chưa chạy kiểm thử.


## Đổi trả, trợ lý và nút trò chuyện góc phải

Đã bổ sung hồ sơ đổi trả cho khách/admin, ảnh minh chứng riêng tư, duyệt/từ chối, hoàn từng phần thực tế, đổi bánh/làm lại và phiếu hỗ trợ hai chiều. Nút tròn màu hồng góc phải dưới mở trợ lý ngay trên trang, dùng chung hội thoại với /assistant, có giá/ảnh/nguồn và chuyển nhân viên qua phiếu. Trợ lý dùng quy tắc và dữ liệu cửa hàng, chưa kết nối AI tạo sinh/LLM.

Máy SPQR/cake_shop đã nâng cấp file 09. Database cũ máy khác chạy 08 rồi 09; cài mới dùng 01. Không chạy lại 01 lên dữ liệu đang dùng. Khởi động lại Spring Boot để nạp mã mới. Hướng dẫn ở docs/DOI-TRA-VA-TRO-LY.md; đối chiếu 5 nhóm ở docs/DOI-CHIEU-5-YEU-CAU.md. Kết quả cuối: 57 bài Java/Spring và 10 JavaScript đạt, SQL Server khởi động/schema đạt; giới hạn ở docs/KET-QUA-KIEM-THU-DOI-TRA-HO-TRO.md.
