document.querySelectorAll('[data-photo]').forEach(button=>button.addEventListener('click',()=>{const image=document.getElementById('mainCakePhoto');if(image)image.src=button.dataset.photo;}));
const choice=document.getElementById('cakeOption');function showPrice(){const selected=choice?.selectedOptions[0];if(selected)document.getElementById('cakePrice').textContent=new Intl.NumberFormat('vi-VN').format(Number(selected.dataset.price))+' đ';}choice?.addEventListener('change',showPrice);showPrice();
const photos=document.getElementById('reviewPhotos');let previews=[];photos?.addEventListener('change',()=>{previews.forEach(URL.revokeObjectURL);previews=[];const box=document.getElementById('reviewPreview');box.replaceChildren();const files=Array.from(photos.files);photos.setCustomValidity(files.length>3?'Chọn tối đa 3 ảnh.':files.some(f=>f.size>5*1024*1024)?'Mỗi ảnh tối đa 5 MB.':'');if(!photos.checkValidity()){photos.reportValidity();return;}files.forEach(file=>{const image=document.createElement('img');const url=URL.createObjectURL(file);previews.push(url);image.src=url;image.alt='Ảnh chuẩn bị gửi';image.className='preview-photo';box.append(image);});});

// Gợi ý tìm kiếm dùng chung trên đầu trang và trang bộ sưu tập.
document.querySelectorAll('.js-cake-search').forEach((input, index) => {
  const field = input.closest('.search-field');
  if (!field) return;
  const panel = document.createElement('div');
  panel.className = 'search-suggestions';
  panel.hidden = true;
  const status = document.createElement('p');
  status.className = 'search-suggestions-status';
  status.setAttribute('role', 'status');
  status.setAttribute('aria-live', 'polite');
  const list = document.createElement('div');
  list.id = `cakeSuggestions-${index}`;
  list.setAttribute('role', 'listbox');
  list.setAttribute('aria-label', 'Bánh gợi ý theo từ khóa');
  const all = document.createElement('a');
  all.className = 'search-see-all';
  panel.append(status, list, all);
  field.append(panel);
  input.setAttribute('role', 'combobox');
  input.setAttribute('aria-autocomplete', 'list');
  input.setAttribute('aria-controls', list.id);
  input.setAttribute('aria-expanded', 'false');
  input.setAttribute('spellcheck', 'false');

  let timer;
  let request;
  let version = 0;
  let active = -1;
  let composing = false;
  const currency = new Intl.NumberFormat('vi-VN');

  function select(position) {
    const links = Array.from(list.querySelectorAll('[role="option"]'));
    active = position;
    links.forEach((link, i) => {
      link.classList.toggle('is-active', i === active);
      link.setAttribute('aria-selected', String(i === active));
    });
    if (links[active]) {
      input.setAttribute('aria-activedescendant', links[active].id);
      links[active].scrollIntoView({ block: 'nearest' });
    } else input.removeAttribute('aria-activedescendant');
  }

  function close() {
    panel.hidden = true;
    input.setAttribute('aria-expanded', 'false');
    select(-1);
  }

  function cancel() {
    clearTimeout(timer);
    request?.abort();
    version++;
    list.removeAttribute('aria-busy');
  }

  function open() {
    if (document.activeElement !== input || !input.value.trim()) return;
    panel.hidden = false;
    input.setAttribute('aria-expanded', 'true');
  }

  function highlightName(name, term) {
    const title = document.createElement('strong');
    title.className = 'search-result-name';
    const text = String(name);
    const position = text.toLocaleLowerCase('vi').indexOf(term.toLocaleLowerCase('vi'));
    if (position < 0) title.textContent = text;
    else {
      const mark = document.createElement('mark');
      mark.textContent = text.slice(position, position + term.length);
      title.append(document.createTextNode(text.slice(0, position)), mark,
        document.createTextNode(text.slice(position + term.length)));
    }
    return title;
  }

  function render(items, term) {
    list.replaceChildren();
    select(-1);
    status.textContent = items.length ? 'Bánh phù hợp với từ khóa của bạn' : 'Chưa tìm thấy bánh phù hợp. Thử một tên bánh khác nhé.';
    items.forEach((cake, i) => {
      const link = document.createElement('a');
      link.id = `${list.id}-${i}`;
      link.className = 'search-result';
      link.href = `${input.dataset.productsUrl}/${encodeURIComponent(cake.id)}`;
      link.setAttribute('role', 'option');
      link.setAttribute('aria-selected', 'false');
      const image = document.createElement('img');
      image.alt = '';
      image.width = 64;
      image.height = 64;
      // Chỉ hiển thị ảnh trong cùng website, gồm ảnh do quản trị tải lên.
      let source;
      try { source = new URL(cake.imageUrl || input.dataset.placeholderUrl, window.location.origin); }
      catch { source = null; }
      image.src = source?.origin === window.location.origin ? source.href : input.dataset.placeholderUrl;
      image.addEventListener('error', () => { image.src = input.dataset.placeholderUrl; }, { once: true });
      const copy = document.createElement('span');
      copy.className = 'search-result-copy';
      const description = document.createElement('span');
      description.className = 'search-result-description';
      description.textContent = cake.description || 'Xem chi tiết chiếc bánh này';
      const price = document.createElement('span');
      price.className = 'search-result-price';
      price.textContent = `${currency.format(cake.price)} đ`;
      copy.append(highlightName(cake.name, term), description, price);
      if (!cake.inStock) {
        const stock = document.createElement('small');
        stock.className = 'search-result-stock';
        stock.textContent = 'Tạm hết hàng';
        copy.append(stock);
      }
      link.append(image, copy);
      link.addEventListener('pointermove', () => select(i));
      list.append(link);
    });
  }

  function schedule() {
    cancel();
    const term = input.value.trim();
    list.replaceChildren();
    select(-1);
    if (!term || composing) { close(); return; }
    const current = version;
    const results = new URL(input.dataset.productsUrl, window.location.origin);
    results.searchParams.set('q', term);
    all.href = results.href;
    all.textContent = `Xem tất cả kết quả cho “${term}” →`;
    status.textContent = 'Đang tìm bánh…';
    list.setAttribute('aria-busy', 'true');
    open();
    timer = setTimeout(async () => {
      request = new AbortController();
      const endpoint = new URL(input.dataset.searchUrl, window.location.origin);
      endpoint.searchParams.set('q', term);
      try {
        const response = await fetch(endpoint, { signal: request.signal, headers: { Accept: 'application/json' } });
        if (!response.ok) throw new Error('Không lấy được gợi ý');
        const items = await response.json();
        if (current !== version || !Array.isArray(items)) return;
        render(items, term);
      } catch (error) {
        if (error.name === 'AbortError' || current !== version) return;
        list.replaceChildren();
        status.textContent = 'Chưa tải được gợi ý. Nhấn Enter hoặc chọn bên dưới để tìm bánh.';
      } finally {
        if (current === version) list.removeAttribute('aria-busy');
      }
    }, 250);
  }

  input.addEventListener('input', () => { if (!composing) schedule(); });
  input.addEventListener('focus', schedule);
  input.addEventListener('compositionstart', () => { composing = true; cancel(); close(); });
  input.addEventListener('compositionend', () => { composing = false; schedule(); });
  input.addEventListener('keydown', event => {
    if (composing || event.isComposing) return;
    if (event.key === 'Escape') { cancel(); close(); return; }
    if (panel.hidden) return;
    const links = list.querySelectorAll('[role="option"]');
    if ((event.key === 'ArrowDown' || event.key === 'ArrowUp') && links.length) {
      event.preventDefault();
      select(event.key === 'ArrowDown' ? (active + 1) % links.length : (active <= 0 ? links.length - 1 : active - 1));
    } else if (event.key === 'Enter' && links[active]) {
      event.preventDefault();
      links[active].click();
    }
  });
  document.addEventListener('pointerdown', event => { if (!field.contains(event.target)) { cancel(); close(); } });
  field.addEventListener('focusout', () => {
    // Đợi trình duyệt chuyển focus để vẫn bấm được kết quả bằng bàn phím.
    setTimeout(() => { if (!field.contains(document.activeElement)) { cancel(); close(); } }, 0);
  });
  input.form?.addEventListener('submit', () => { cancel(); close(); });
});
