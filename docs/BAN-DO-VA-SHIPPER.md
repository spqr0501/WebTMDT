# Bản đồ địa chỉ, shipper và logo cửa hàng

## Địa chỉ nhận bánh

Trang **Tài khoản → Địa chỉ giao hàng** có nút mở bản đồ và lấy vị trí hiện tại. Cho phép quyền vị trí, kiểm tra ghim rồi xem địa chỉ được điền. Có thể bấm trên bản đồ hoặc kéo ghim, sau đó bấm **Điền địa chỉ tại ghim**. Tìm bằng nút **Tìm trên bản đồ**, không gọi dịch vụ liên tục khi đang gõ.

Ở thanh toán, chọn **Dùng địa chỉ mới / lấy vị trí hiện tại cho đơn này** để nhập người nhận, điện thoại và dùng cùng bản đồ. Địa chỉ này dùng riêng cho đơn, không tự thêm vào sổ địa chỉ. Nếu chọn địa chỉ đã lưu, đơn sao chép tọa độ của địa chỉ đó; thay đổi sổ địa chỉ sau này không thay đổi điểm giao của đơn cũ. Địa chỉ cũ chưa có tọa độ vẫn đặt hàng được.

Địa chỉ từ GPS có thể thiếu số nhà hoặc sai vị trí; phải kiểm tra và bổ sung tầng, hẻm, hướng dẫn giao hàng. Tra cứu lỗi hoặc không cho phép vị trí vẫn nhập và đặt bằng địa chỉ chữ được. Nhận tại cửa hàng không yêu cầu vị trí giao.

## Quản lý và phân công shipper

1. Admin mở **Quản lý shipper** (`/admin/shippers`) → Thêm shipper, nhập tên, email, điện thoại, phương tiện/biển số, mật khẩu từ 8 ký tự. Không có tài khoản shipper mẫu hoặc mật khẩu dùng chung.
2. Tại chi tiết đơn giao tận nơi đã xác nhận/đang chuẩn bị, chọn shipper phụ trách rồi lưu. Khi đang giao có thể chuyển trực tiếp sang shipper khác. Vị trí cũ được xóa khi đổi người giao.
3. Shipper đăng nhập tài khoản được cấp, chọn **Đơn giao của tôi** (`/shipper`). Chỉ thấy đơn được phân công. Sau khi được bàn giao bánh ở trạng thái đang chuẩn bị, bấm **Đã nhận bánh · Bắt đầu giao**.
4. Bấm **Bật chia sẻ GPS** và cho phép vị trí để gửi GPS cho đơn này. Vị trí được gửi khoảng 15 giây/lần, không vẽ đường mô phỏng. Có nút dừng chia sẻ. Chuyển tab/ẩn trang sẽ dừng lấy GPS; quay lại cần bật lại. Điện thoại phải giữ trang ở phía trước; website không bảo đảm theo dõi khi khóa màn hình hoặc trình duyệt chạy nền.
5. Đã bàn giao đủ bánh thì **Báo đã giao thành công**. GPS được xóa/ẩn, khách và admin có thông báo; admin kiểm tra giao nhận, đối soát khoản thu thực tế rồi chuyển đơn sang hoàn thành. Báo giao không tự tăng số tiền đã thanh toán. Giao thất bại thì ghi sự cố gửi cửa hàng xử lý, không tự hủy hay hoàn tiền.

Ngừng shipper thay cho xóa để giữ lịch sử. Cần phân công lại đơn đang phụ trách trước khi khóa/ngừng tài khoản. Thay đổi thông tin đăng nhập sẽ làm phiên shipper cũ hết hiệu lực.

## Khách theo dõi

Trong chi tiết đơn, bấm **Theo dõi shipper trên bản đồ**. Chỉ chủ đơn, admin và shipper được phân công có quyền xem. Hiện tên, số điện thoại, phương tiện, điểm nhận bánh nếu có ghim và vị trí shipper đang chia sẻ. Dữ liệu được cập nhật khoảng 15 giây khi trang đang mở; sau 90 giây không có vị trí mới sẽ ghi rõ vị trí đã cũ.

Chỉ lưu vị trí gần nhất cho từng đơn, không lưu toàn bộ lịch sử di chuyển. Không hiện tọa độ shipper khi chưa bật chia sẻ, đơn kết thúc hoặc đã báo bàn giao. Mỗi lần bật có phiên chia sẻ riêng; bản tin đến trễ từ phiên đã dừng/đổi shipper bị từ chối. Không có vị trí GPS giả, ước tính giờ đến hay chỉ đường. Nếu mất mạng, trang ghi rõ chưa xác nhận được vị trí mới.

Luồng phân công và theo dõi hiện áp dụng cho **đơn bánh trong giỏ hàng giao tận nơi** (`/orders`). Yêu cầu bánh thiết kế riêng (`/custom-cakes`) giữ quy trình riêng hiện có, chưa có phân công/GPS shipper.

## Bản đồ và kết nối

Leaflet 1.9.4 được lưu tại project (kèm giấy phép). Ô bản đồ tải nền từ OpenStreetMap và ghi nguồn; tra địa chỉ qua Photon. Chỉ gọi tra cứu khi người dùng bấm nút hoặc lấy vị trí, có giới hạn toàn ứng dụng khoảng 1 lần/giây và bộ nhớ đệm 10 phút (tối đa 512 kết quả). Không gửi tên, số điện thoại hoặc mã đơn đến dịch vụ tra cứu. Tọa độ/nội dung tìm địa chỉ được gửi khi người dùng yêu cầu tra cứu; việc này được nêu ngay trong giao diện.

Photon công cộng chỉ phù hợp đồ án/lượng yêu cầu vừa phải, không bảo đảm sẵn sàng; khi triển khai thật cần dịch vụ riêng hoặc nhà cung cấp có cam kết. Có thể đổi `MAP_GEOCODER_BASE_URL` sang máy chủ tương thích Photon và `MAP_TILES_URL` sang dịch vụ nền bản đồ phù hợp, không cần sửa mã. Tuân theo [điều kiện Photon](https://github.com/komoot/photon#demo-server) và [chính sách nền OpenStreetMap](https://operations.osmfoundation.org/policies/tiles/); không tải hàng loạt hoặc ẩn ghi nguồn.

GPS trình duyệt cần HTTPS hoặc localhost và sự cho phép của người dùng. Điện thoại truy cập `http://địa-chỉ-IP-máy-tính:8080` thường không dùng được GPS; cần địa chỉ HTTPS. Xem [tài liệu Geolocation](https://developer.mozilla.org/en-US/docs/Web/API/Geolocation_API).

## Database và logo

Đã bổ sung cột tọa độ/shipper trên database SPQR, giữ dữ liệu cũ. File SQL chính thức vẫn là `database/BONG_BAKERY_SQL_SERVER.sql`, đã tích hợp **phần 13**. Cài mới chạy cả file; máy khác có database cũ chỉ chạy phần 13 sau các nâng cấp trước đó, không chạy lại toàn bộ file tạo mới.

Logo dùng nguyên ảnh cuối do chủ cửa hàng cung cấp, lưu tại `src/main/resources/static/images/logo-bong-bakery.png`. Đã dùng ở đầu trang, chân trang, khu quản trị, ảnh lớn trên trang chủ, biểu tượng tab và hóa đơn HTML. Không tạo lại hoặc chỉnh sửa ảnh logo.

Lần thay đổi này chỉ biên dịch mã để kiểm tra khả năng xây dựng, **chưa chạy kiểm thử hoặc nghiệm thu trình duyệt** theo yêu cầu chỉ kiểm thử khi chủ dự án yêu cầu.

## Cập nhật kiểm thử 28/10/2026

Đã chạy kiểm thử theo yêu cầu mới: 87 Java/Spring và 24 JavaScript đạt. Có kiểm tra bản đồ/giới hạn số lượng/phân trang trên trình duyệt. Xem [báo cáo đầy đủ và các giới hạn](KET-QUA-KIEM-THU-2026-10-28.md). Ghi chú chưa kiểm thử bên trên mô tả thời điểm bàn giao trước lượt kiểm tra này.
