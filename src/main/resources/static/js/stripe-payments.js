(() => {
 'use strict';
 document.querySelectorAll('form[data-stripe-submit]').forEach(form=>form.addEventListener('submit',()=>{form.querySelectorAll('button').forEach(b=>{b.disabled=true;b.textContent='Đang xử lý…';});}));
 const box=document.querySelector('[data-stripe-result]');if(!box||!['CREATING','OPEN'].includes(box.dataset.initialState))return;
 const status=box.querySelector('[data-stripe-poll-message]');let busy=false,count=0;
 const timer=setInterval(async()=>{if(document.hidden||busy)return;if(count++>=60){clearInterval(timer);status.textContent='Chưa nhận kết quả mới. Hãy kiểm tra kết nối webhook hoặc bấm Làm mới sau.';return;}busy=true;const controller=new AbortController(),timeout=setTimeout(()=>controller.abort(),8000);try{const r=await fetch(box.dataset.stateUrl,{headers:{Accept:'application/json'},cache:'no-store',signal:controller.signal});if(!r.ok||!r.headers.get('content-type')?.includes('application/json'))throw Error();const p=await r.json();if(p.final){clearInterval(timer);window.location.reload();return;}status.textContent='Đang chờ xác nhận từ Stripe…';}catch(e){status.textContent='Chưa lấy được kết quả mới. Không thanh toán lại ngay nếu bạn đã trả tiền trên Stripe.';}finally{clearTimeout(timeout);busy=false;}},3000);window.addEventListener('pagehide',()=>clearInterval(timer));
})();
