(() => {
 'use strict';
 document.querySelectorAll('[data-favorite-form]').forEach(form=>{
  form.addEventListener('submit',async event=>{
   if(!window.fetch)return;
   event.preventDefault();const button=form.querySelector('button');if(button.disabled)return;
   button.disabled=true;const status=form.querySelector('[role="status"]');
   try{const response=await fetch(form.dataset.stateUrl,{method:'POST',credentials:'same-origin',headers:{Accept:'application/json'},body:new FormData(form)});if(response.redirected||!response.ok||!(response.headers.get('content-type')||'').includes('application/json'))throw new Error();const data=await response.json();
    document.querySelectorAll('[data-favorite-form]').forEach(other=>{if(other.dataset.productId!==form.dataset.productId)return;const b=other.querySelector('button');b.classList.toggle('is-favorite',data.favorite);b.setAttribute('aria-pressed',String(data.favorite));b.setAttribute('aria-label',data.favorite?'Bỏ bánh khỏi yêu thích':'Thêm bánh vào yêu thích');b.title=data.favorite?'Bỏ yêu thích':'Thêm vào yêu thích';other.querySelector('[name="favorite"]').value=String(!data.favorite);});status.textContent=data.message;if(!data.favorite&&form.querySelector('[name="back"]').value==='favorites')window.location.reload();
   }catch{status.classList.remove('visually-hidden');status.textContent='Chưa lưu được. Hãy tải lại trang và đăng nhập nếu phiên đã hết.';}
   finally{button.disabled=false;}
  });
 });
 document.querySelectorAll('[data-confirm-accessory-delete]').forEach(form=>form.addEventListener('submit',event=>{if(!window.confirm('Xóa phụ kiện này? Nếu đã có trong đơn hàng, phụ kiện sẽ được chuyển sang ngừng bán.'))event.preventDefault();}));
})();
