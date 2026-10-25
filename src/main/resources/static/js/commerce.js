// Nhận tại cửa hàng / giao tận nơi: chỉ gửi các trường thuộc lựa chọn hiện tại.
document.querySelectorAll('[data-fulfillment]').forEach(select => {
  const form = select.closest('form');
  const change = () => {
    const pickup = select.value === 'PICKUP';
    form.querySelectorAll('[data-delivery-fields], [data-pickup-fields]').forEach(group => {
      const visible = group.hasAttribute('data-pickup-fields') ? pickup : !pickup;
      group.hidden = !visible;
      group.querySelectorAll('input,textarea,select').forEach(field => { field.disabled = !visible; });
    });
  };
  select.addEventListener('change', change);
  change();
});

const preorder = document.getElementById('preorder');
const receiveDate = document.getElementById('deliveryDate');
if (preorder && receiveDate) {
  const change = () => {
    receiveDate.max = preorder.checked ? receiveDate.dataset.preorderMax : receiveDate.dataset.normalMax;
  };
  preorder.addEventListener('change', change);
  change();
}

const checkout = document.getElementById('checkoutForm');
checkout?.addEventListener('input', event => {
  if (!event.target.matches('[name^="accessories["], [name="fulfillmentMethod"], [name="voucherCode"], [name="paymentPlan"], [name="paymentMethod"]')) return;
  const hint = document.getElementById('checkoutChangeHint');
  if (hint) hint.hidden = false;
  const place = checkout.querySelector('button[value="place"]');
  if (place) place.disabled = true;
});

// Chỉ cập nhật số thông báo; không tự đánh dấu đã đọc hay phát âm thanh.
const counters = Array.from(document.querySelectorAll('.js-notification-count'));
let notificationLoading = false;
async function refreshNotifications() {
  if (!counters.length || notificationLoading || document.hidden) return;
  notificationLoading = true;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 10000);
  try {
    const response = await fetch(counters[0].dataset.countUrl, {
      headers: { Accept: 'application/json' }, signal: controller.signal, cache: 'no-store'
    });
    if (!response.ok || !response.headers.get('content-type')?.includes('application/json')) return;
    const data = await response.json();
    const count = Number(data.unread);
    if (!Number.isFinite(count) || count < 0) return;
    counters.forEach(counter => {
      counter.textContent = count > 99 ? '99+' : String(count);
      counter.hidden = count === 0;
      counter.setAttribute('aria-label', `${count} thông báo chưa đọc`);
    });
  } catch { /* Giữ số gần nhất; người dùng vẫn mở được trang thông báo. */ }
  finally { clearTimeout(timeout); notificationLoading = false; }
}
if (counters.length) {
  setInterval(refreshNotifications, 30000);
  document.addEventListener('visibilitychange', () => { if (!document.hidden) refreshNotifications(); });
}
