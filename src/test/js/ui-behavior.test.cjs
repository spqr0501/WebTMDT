// Kiểm tra JavaScript cục bộ bằng Node.js, không mở trình duyệt hay gọi mạng.
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const root = path.resolve(__dirname, '../../main/resources/static/js');
const source = name => fs.readFileSync(path.join(root, name), 'utf8');
function element(extra = {}) {
  return Object.assign({ events: {}, dataset: {}, hidden: false,
    addEventListener(event, callback) { this.events[event] = callback; },
    setAttribute(name, value) { this[name] = value; },
    classList: { values: new Set(), add(name) { this.values.add(name); } }
  }, extra);
}
function context(selectors = {}, ids = {}, storage) {
  const document = { documentElement: { dataset: {} }, hidden: false,
    querySelectorAll: selector => selectors[selector] || [],
    getElementById: id => ids[id] || null, addEventListener() {} };
  return vm.createContext({ document, sessionStorage: storage || { getItem: () => null, setItem() {} },
    window: { location: { pathname: '/admin/products/12/edit' } },
    URL, Intl, Date, setTimeout, clearTimeout, setInterval: () => { throw Error('Không được gọi mạng trong bài kiểm tra cục bộ'); } });
}
test('Toàn bộ tệp JavaScript giao diện có cú pháp hợp lệ', () => {
  for (const file of fs.readdirSync(root).filter(name => name.endsWith('.js'))) {
    assert.doesNotThrow(() => new vm.Script(source(file), { filename: file }));
  }
});
test('Nút đóng ưu đãi ẩn thanh và giữ lựa chọn khi chuyển trang', () => {
  const values = new Map();
  const storage = { getItem: key => values.get(key), setItem: (key, value) => values.set(key, value) };
  const banner = element();const close = element({ closest: () => banner });
  const first = context({ '[data-dismiss-announcement]': [close] }, {}, storage);
  vm.runInContext(source('theme-init.js'), first);vm.runInContext(source('theme.js'), first);
  close.events.click();assert.equal(banner.hidden, true);
  const next = context({}, {}, storage);vm.runInContext(source('theme-init.js'), next);
  assert.equal(next.document.documentElement.dataset.promotionDismissed, 'true');
  const blocked = context({ '[data-dismiss-announcement]': [close] }, {}, {
    getItem() { throw Error('Storage bị chặn'); }, setItem() { throw Error('Storage bị chặn'); }
  });
  assert.doesNotThrow(() => { vm.runInContext(source('theme-init.js'), blocked);vm.runInContext(source('theme.js'), blocked);close.events.click(); });
});
test('Trang sửa bánh làm nổi bật mục sản phẩm, không chọn nhầm Tổng quan', () => {
  const overview = element({ href: 'https://example.invalid/admin' });
  const products = element({ href: 'https://example.invalid/admin/products' });
  const orders = element({ href: 'https://example.invalid/admin/orders' });
  vm.runInContext(source('theme.js'), context({ '.admin-sidebar-menu a.nav-link': [overview, products, orders] }));
  assert.equal(products.classList.values.has('active'), true);
  assert.equal(products['aria-current'], 'page');assert.equal(overview.classList.values.has('active'), false);
});
test('Nhận tại cửa hàng bỏ trường giao hàng; đặt trước mở giới hạn 90 ngày', () => {
  const address = element();const recipient = element();
  const delivery = element({ hasAttribute: () => false, querySelectorAll: () => [address] });
  const pickup = element({ hasAttribute: () => true, querySelectorAll: () => [recipient] });
  const form = { querySelectorAll: () => [delivery, pickup] };
  const method = element({ value: 'DELIVERY', closest: () => form });
  const preorder = element({ checked: false });
  const date = element({ dataset: { normalMax: '2026-11-24', preorderMax: '2027-01-23' } });
  const sandbox = context({ '[data-fulfillment]': [method] }, { preorder, deliveryDate: date });
  vm.runInContext(source('commerce.js'), sandbox);
  assert.equal(address.disabled, false);assert.equal(recipient.disabled, true);assert.equal(date.max, '2026-11-24');
  method.value = 'PICKUP';method.events.change();assert.equal(address.disabled, true);assert.equal(delivery.hidden, true);assert.equal(recipient.disabled, false);assert.equal(pickup.hidden, false);
  preorder.checked = true;preorder.events.change();assert.equal(date.max, '2027-01-23');
});
