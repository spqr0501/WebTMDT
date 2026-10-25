document.querySelectorAll('[data-dismiss-announcement]').forEach(button => {
  button.addEventListener('click', () => {
    document.documentElement.dataset.promotionDismissed = 'true';
    const banner = button.closest('[data-announcement]');
    if (banner) banner.hidden = true;
    try { sessionStorage.setItem('bongbakery:promotion-dismissed', '1'); } catch { }
  });
});

// Làm nổi bật đúng mục quản trị, kể cả khi đang sửa hoặc xem chi tiết.
const sidebarLinks = Array.from(document.querySelectorAll('.admin-sidebar-menu a.nav-link'));
const currentPath = window.location.pathname.replace(/\/$/, '');
const matched = sidebarLinks.filter(link => {
  const path = new URL(link.href).pathname.replace(/\/$/, '');
  return currentPath === path || currentPath.startsWith(`${path}/`);
}).sort((a, b) => new URL(b.href).pathname.length - new URL(a.href).pathname.length)[0];
if (matched) {
  matched.classList.add('active');
  matched.setAttribute('aria-current', 'page');
}
document.querySelectorAll('[data-today-label]').forEach(label => {
  label.textContent = new Intl.DateTimeFormat('vi-VN', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  }).format(new Date());
});
