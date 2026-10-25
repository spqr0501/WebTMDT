# Hóa đơn và cọc 50% tại Bóng Bakery

## Cập nhật dự án

File `database/07-thanh-toan-coc-va-hoa-don.sql` đã được áp dụng trên database `cake_shop` của SPQR. Máy khác nâng cấp bằng file 07 sau các phần trước. Cài mới dùng file 01 đã bao gồm phần bổ sung.

Trong Eclipse, Refresh dự án → Maven → Update Project để nhận thư viện PDFBox → khởi động lại Spring Boot App → Ctrl + F5.

## Nhận tại cửa hàng

- Khách chọn **cọc 50%** hoặc **trả đủ trước**, chuyển khoản theo hướng dẫn của cửa hàng. Đơn nhận tại cửa hàng không chọn thanh toán toàn bộ khi đến lấy nữa.
- Tiền cọc được tính trên tổng cuối cùng sau giảm giá, bao gồm phụ kiện. Nếu tổng lẻ một đồng, phần cọc làm tròn lên đến đồng.
- Đặt hàng không tự đánh dấu đã trả tiền. Admin mở đơn → Thanh toán & chứng từ → Đối soát và ghi nhận giao dịch thực tế, nhập số tiền và mã tham chiếu/biên nhận.
- Chỉ khi số tiền đã ghi nhận đạt yêu cầu trả trước, Admin mới chuyển đơn sang Đã xác nhận / Đang chuẩn bị.
- Nếu khách cọc, phần còn lại được hiển thị trong đơn và hóa đơn. Admin ghi nhận phần còn lại trước khi đánh dấu đơn nhận tại cửa hàng hoàn thành. Tiền còn lại có thể được nhận khi khách đến lấy, sau đó ghi nhận đối soát.
- Đơn giao tận nơi vẫn có COD hoặc chuyển khoản. Với chuyển khoản, phải ghi nhận đủ tiền trước khi xác nhận / chuẩn bị; COD không yêu cầu khoản trả trước.

## Bánh thiết kế riêng

Khách nhận tại cửa hàng chọn cọc 50% hoặc trả đủ ngay trên yêu cầu. Khi Admin báo giá, hệ thống tính số cần trả trước từ tổng báo giá. Khách đồng ý báo giá trước, sau đó chuyển tiền; Admin đối soát đủ tiền mới bắt đầu chuẩn bị. Hóa đơn/PDF chỉ khả dụng khi đã có báo giá.

## Giao dịch và hoàn tiền

- Mỗi lần đối soát có số tiền, loại nhận/hoàn, tham chiếu, người xác nhận và thời điểm.
- Mã gửi biểu mẫu chống ghi nhận trùng khi gửi lại cùng một thao tác. Khóa đơn và kiểm tra số tiền ở máy chủ giúp tránh ghi vượt tổng hoặc hoàn vượt tiền đang giữ.
- Người dùng không có quyền tự sửa số đã trả. Chỉ Admin đang hoạt động được ghi nhận sau khi kiểm tra tiền thực tế.
- Hủy đơn không tự trả tiền qua ngân hàng và không làm mất lịch sử thanh toán. Admin chỉ ghi nhận hoàn sau khi đã thực sự chuyển lại tiền; khoản hoàn không được vượt số đã nhận chưa hoàn.
- Đơn cũ được giữ nguyên, không tự giả định đã thu tiền. Nếu cần, Admin đối soát giao dịch cũ và nhập đúng số thực tế trước khi hoàn thành đơn nhận tại cửa hàng.
- Các chỉ số doanh thu hiện tại tiếp tục dựa trên đơn hoàn thành, không phải báo cáo dòng tiền đã thu. Nhật ký đối soát thể hiện số thực nhận/hoàn của từng đơn.

## Hóa đơn và PDF

- Khách: Đơn hàng → Chi tiết → Xem hóa đơn / Tải PDF. Bánh riêng có nút tương tự sau khi được báo giá.
- Admin: Quản lý đơn hàng hoặc Bánh thiết kế riêng → Chi tiết → Xem hóa đơn / Tải PDF.
- Người dùng chỉ được xem/tải chứng từ thuộc tài khoản của mình; đường dẫn quản trị yêu cầu quyền ADMIN.
- Chứng từ có mã `BB-...` hoặc `BB-TK-...`, thông tin người nhận, lịch nhận, sản phẩm và phụ kiện, số lượng, giá lúc đặt, giảm giá, phí giao, tổng tiền, kế hoạch cọc, số đã ghi nhận, số còn lại và lịch sử đối soát.
- Đơn bị hủy được ghi rõ, khoản tiền cửa hàng còn giữ được hiển thị là cần đối soát hoàn. Báo giá bánh riêng đang chờ khách đồng ý có tiêu đề báo giá.
- PDF được tạo ở máy chủ với phông Noto Sans có đủ dấu tiếng Việt, hỗ trợ nhiều trang khi danh sách dài. Tệp tải về riêng theo quyền truy cập và không lưu vào thư mục media công khai.
- Đây là chứng từ bán hàng nội bộ; chưa kết nối nhà cung cấp hóa đơn điện tử VAT.

Thư viện: [Apache PDFBox 3](https://pdfbox.apache.org/3.0/getting-started.html). Phông Noto Sans kèm giấy phép SIL Open Font License trong `src/main/resources/fonts`.

## Dòng hướng dẫn chuyển khoản

Giá trị mặc định trong `application.properties` đã dùng Unicode escapes để Spring đọc đúng dấu tiếng Việt, kể cả khi bộ nạp `.properties` dùng ISO-8859-1. Nếu tự đặt `BANK_INSTRUCTIONS` trong cấu hình chạy Eclipse, nhập trực tiếp nội dung tiếng Việt đúng; không dán chuỗi đã lỗi dấu.

Đã kiểm thử trong đợt đổi trả/trợ lý ngày 25/10/2026: sinh/đọc PDF tiếng Việt và khoản hoàn, quyền truy cập đều đạt. Xem KET-QUA-KIEM-THU-DOI-TRA-HO-TRO.md; chưa kiểm tra bố cục PDF bằng render từng trang.


Hoàn tiền sau giao: cần hồ sơ đổi trả được duyệt, ghi khoản thực trả tại hồ sơ. Tổng đã hoàn lưu riêng với tiền ròng, không biến khoản hoàn thành nợ khách phải trả lại. Doanh thu đơn hoàn thành được trừ khoản hoàn thực tế. Xem DOI-TRA-VA-TRO-LY.md.
