// Đọc trước khi vẽ trang để thanh đã đóng không nháy lại khi chuyển trang.
try {
  if (sessionStorage.getItem('bongbakery:promotion-dismissed') === '1') {
    document.documentElement.dataset.promotionDismissed = 'true';
  }
} catch { /* Nút đóng vẫn dùng được nếu trình duyệt không cho lưu tùy chọn. */ }
