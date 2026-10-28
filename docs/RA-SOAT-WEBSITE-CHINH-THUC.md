# Rà soát để vận hành Bóng Bakery chính thức

Rà soát mã nguồn và cấu hình ngày 28/10/2026. Đây là đánh giá phạm vi chức năng, không phải nghiệm thu vận hành, kiểm thử bảo mật hoặc kết luận tuân thủ pháp luật. Không chạy kiểm thử trong lần rà soát này.

## Nền tảng đã có

Website có trang chủ, danh mục/tìm kiếm, chi tiết bánh/đánh giá, giỏ, thanh toán, voucher, nhận tại cửa hàng, cọc, đặt trước, bánh riêng/duyệt mẫu, đơn hàng, hóa đơn/PDF, yêu thích, trang cá nhân/sổ địa chỉ, thông báo, yêu cầu đổi trả, phiếu hỗ trợ và trợ lý theo dữ liệu cửa hàng. Admin có CRUD, kho, báo cáo/PDF, Excel, lịch sản xuất, đối soát, QR nhận bánh và shipper. Đã có trang giới thiệu, liên hệ, giao nhận, bảo quản, chính sách voucher/hủy/đổi trả.

Không cần thêm lại các trang đã tồn tại chỉ để tăng số trang. Nên hoàn thiện dữ liệu, luồng giao nhận và độ tin cậy trước.

## Các trang nên bổ sung

| Trang/mục | Nội dung thực tế cần chuẩn bị | Ưu tiên |
|---|---|---|
| Chính sách bảo mật và dữ liệu cá nhân | Dữ liệu thu thập, mục đích, lưu giữ/xóa, chia sẻ địa chỉ/GPS với dịch vụ bản đồ và shipper, liên hệ hỗ trợ quyền riêng tư | Trước khi mở công khai |
| Điều khoản mua hàng | Chủ thể bán hàng, cách xác nhận đơn, thay đổi mẫu bánh, thanh toán, giao nhận, khiếu nại; khớp đúng các chính sách đang vận hành | Trước khi bán thật |
| Câu hỏi thường gặp | Thời gian đặt bánh, đổi mẫu, giao trễ, lấy tại cửa hàng, tiền cọc, bảo quản, dị ứng, mất kết nối theo dõi | Nên có |
| Liên hệ / thông tin cửa hàng | Hoàn thiện trang hiện có bằng địa chỉ, điện thoại, giờ mở cửa, email thật và điểm nhận bánh đã xác nhận | Trước khi bán thật |
| Cấu hình cửa hàng trong admin | Giờ hoạt động/ngày nghỉ, khu vực giao, phí giao, thông tin nhận chuyển khoản và nội dung liên hệ thay cho sửa cấu hình thủ công | Nên có |

Không tự điền số tài khoản, danh tính kinh doanh, địa chỉ hoặc cam kết xử lý khi chưa có thông tin thật từ cửa hàng. Những nội dung liên quan pháp lý cần chủ cửa hàng xác nhận phù hợp với mô hình kinh doanh thực tế.

## Các việc còn cần trước khi vận hành

1. **Email và thông tin thật:** SMTP hiện mặc định tắt; hoàn thiện gửi liên kết quên mật khẩu, xác minh email và thư xác nhận đơn. Cấu hình `SHOP_PHONE`, `SHOP_ADDRESS`, `SHOP_HOURS`, thông tin nhận chuyển khoản thực tế; mặc định trong mã chưa đầy đủ, môi trường chạy có thể đã cung cấp giá trị riêng.
2. **Triển khai:** tên miền/HTTPS, cookie bảo mật theo môi trường, sao lưu và khôi phục SQL Server cùng thư mục upload, theo dõi lỗi và cảnh báo vận hành. GPS điện thoại cần HTTPS. Cấu hình phát triển SQL Server đang cho phép tin chứng chỉ máy chủ tự ký; cần cấu hình phù hợp khi triển khai thật.
3. **Tài khoản và truy cập:** tăng bảo vệ đăng nhập, giới hạn thử sai và xác minh bổ sung cho admin. Tách quyền chăm sóc khách/kho/giao hàng nếu có nhiều nhân viên. Xác thực lại khi đổi dữ liệu đăng nhập quan trọng. Tham khảo [khuyến nghị xác thực của OWASP](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html).
4. **Bán và giao bánh thật:** xác nhận khu vực phục vụ, chặn địa chỉ ngoài vùng, công suất theo ngày/khung giờ, cách xử lý giao thất bại. Hiện phân công/GPS shipper áp dụng đơn giỏ hàng, chưa nối vào luồng bánh thiết kế riêng. Dịch vụ tra địa chỉ Photon công cộng chỉ phù hợp lượng yêu cầu vừa phải, không có cam kết sẵn sàng.
5. **Thanh toán:** hiện chuyển khoản được admin đối soát thủ công; cần chọn cách vận hành này rõ ràng hoặc tích hợp nhà cung cấp có thông báo giao dịch, kiểm tra chữ ký và chống ghi nhận trùng. Không đánh dấu đã thanh toán chỉ từ trang chuyển hướng thành công.
6. **Dữ liệu sản phẩm:** thay ảnh minh họa bằng ảnh sản phẩm thực tế, xác nhận kích thước, số phần, thành phần, dị ứng, hạn dùng và bảo quản. Không suy đoán mức an toàn thực phẩm từ ảnh.
7. **Nghiệm thu:** sau khi được yêu cầu, kiểm thử đầy đủ từ tìm bánh → đặt → thanh toán → phân công → giao → hoàn thành → đánh giá/đổi trả; bổ sung trường hợp tranh chấp tồn cuối cùng, quyền truy cập giữa tài khoản, GPS thật, email thật, điện thoại và khôi phục bản sao lưu. Các kiểm thử trước ngày thêm trang cá nhân/shipper không thay cho nghiệm thu phiên bản mới.

## Ý tưởng nâng cấp tiếp theo

- Kho theo lô/ngày sản xuất/hạn dùng, xuất lô gần hết hạn trước, phân biệt hàng bán sẵn và năng lực làm bánh theo lịch.
- Giới hạn số bánh hoặc công suất từng khung giờ, có ngày nghỉ và thời gian chốt đơn.
- Địa chỉ mặc định; giữ giỏ giữa các lần đăng nhập và thiết bị, thông báo bánh yêu thích có hàng lại khi khách đăng ký nhận.
- Tích điểm, hạng khách thân thiết và ưu đãi sinh nhật nếu khách tự nguyện cung cấp ngày sinh.
- Trang tìm kiếm thân thiện công cụ tìm kiếm: tiêu đề từng bánh, đường dẫn chuẩn, sitemap và dữ liệu sản phẩm/giá/tồn. Google có thể dùng dữ liệu cấu trúc để hiển thị giá, khả năng còn hàng và chính sách giao/đổi trả; không bảo đảm mọi trang đều xuất hiện dạng mở rộng. Xem [tài liệu Google về sản phẩm thương mại](https://developers.google.com/search/docs/appearance/structured-data/merchant-listing).

Đề xuất thứ tự tiếp theo: **bảo mật/điều khoản + thông tin cửa hàng → email → khu vực/công suất giao nhận → nghiệm thu thực tế → tìm kiếm Google và chăm sóc khách hàng**.
