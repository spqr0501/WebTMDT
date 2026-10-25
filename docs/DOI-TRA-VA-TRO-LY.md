# Sử dụng đổi trả và trợ lý

## Khách hàng

- Chi tiết đơn thường hoặc bánh riêng COMPLETED → Gửi yêu cầu đổi trả. Trong 24 giờ từ bàn giao, nhập mô tả và 1–3 ảnh JPG/PNG. Theo dõi ở Tài khoản → Đổi trả & hoàn tiền.
- Đơn cũ thiếu giờ bàn giao hoặc quá hạn: Gửi phiếu hỗ trợ để xem xét riêng. Gửi lại hồ sơ đã có sẽ mở hồ sơ cũ.
- Bấm **nút tròn màu hồng góc phải dưới** để trò chuyện ngay trên trang: gợi ý bánh theo ngân sách, hỏi thành phần/bảo quản hoặc chính sách; có nguồn và thời điểm tra. Bấm × hoặc Esc để đóng, có trang đầy đủ /assistant.
- Gửi phiếu cho nhân viên khi cần. Mặc định không đính hội thoại; chọn ô đồng ý mới gửi kèm. Theo dõi trả lời và thông báo ở Tài khoản → Phiếu hỗ trợ. Không phải cuộc chat trực tiếp.

## Quản trị

- Menu Đổi trả & hoàn tiền → Chờ xem xét → xem ảnh và đối chiếu đơn/mẫu.
- Nhập phản hồi, duyệt hoàn/đổi bánh hoặc từ chối có lý do. Tiền duyệt không vượt tiền đang giữ; đối soát thu tiền trước.
- Hoàn tiền: chuyển thực tế bên ngoài rồi ghi từng khoản và tham chiếu; chặn vượt duyệt/vượt tiền đang giữ/ghi lặp. Đủ khoản duyệt tự hoàn tất.
- Đổi bánh/làm lại: trao đổi lịch, xuất bánh thay thế theo điều chỉnh kho có lý do, sau giao mới xác nhận. Không nhập lại bánh lỗi vào kho bán.
- Menu Phiếu hỗ trợ → trả lời và chọn trạng thái; khách bổ sung sẽ mở lại chờ tiếp nhận.
- Dashboard có số hồ sơ/phiếu chờ. Doanh thu trừ thực hoàn; hóa đơn giữ tổng gốc và hiện khoản hoàn.

## Phạm vi

Trợ lý dùng quy tắc và dữ liệu nội bộ, **không phải AI tạo sinh, chưa nối LLM**, không cần API key/không gửi hội thoại ra ngoài. Tra tối đa 200 bánh đang bán mới nhất, 4 kết quả, tối đa 12 lượt và 600 ký tự/câu. Không tự bịa thành phần hay cam kết an toàn dị ứng. Nếu môn học bắt buộc một mô hình AI thực sự thì cần tích hợp thêm.

## Triển khai

SPQR/cake_shop đã nâng cấp file 09. Máy khác có database cũ chạy 08 rồi 09; cài mới dùng 01, không chạy lại 01 để nâng cấp. Ảnh minh chứng ở uploads/private, sao lưu cùng DB/uploads và không public thư mục qua server khác. Eclipse: Maven → Update Project nếu cần, Run As → Spring Boot App. Khung nổi dùng header/footer chung, không cần chép vào từng trang.
