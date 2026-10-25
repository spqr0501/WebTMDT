(() => {
 'use strict';
 const root=document.querySelector('[data-assistant-widget]');
 if(!root)return;
 const toggle=root.querySelector('[data-assistant-toggle]'),panel=root.querySelector('#bakeryAssistantPanel');
 const close=root.querySelector('[data-assistant-close]'),form=root.querySelector('[data-assistant-form]');
 const input=root.querySelector('[data-assistant-input]'),submit=root.querySelector('[data-assistant-submit]');
 const log=root.querySelector('[data-assistant-messages]'),error=root.querySelector('[data-assistant-error]');
 const authenticated=root.dataset.authenticated==='true';
 let loaded=false,busy=false;
 const node=(tag,className,text)=>{const element=document.createElement(tag);if(className)element.className=className;if(text!==undefined)element.textContent=String(text);return element;};
 const safeUrl=value=>{try{const url=new URL(value,window.location.href);return url.origin===window.location.origin&&url.pathname.startsWith('/')?url.pathname+url.search+url.hash:null;}catch{return null;}};
 const showError=message=>{error.textContent=message;error.hidden=!message;};
 const scroll=()=>{log.scrollTop=log.scrollHeight;};
 const question=text=>{log.append(node('div','assistant-bubble assistant-bubble-user',text));scroll();};
 const answer=data=>{
  const bubble=node('article','assistant-bubble assistant-bubble-bot');bubble.append(node('span','assistant-bubble-label','Trợ lý Bóng Bakery'));bubble.append(node('p','assistant-answer-text',data.text));
  for(const cake of data.cakes||[]){const href=safeUrl(cake.url);if(!href)continue;const link=node('a','assistant-widget-cake');link.href=href;const image=node('img');const imageUrl=safeUrl(cake.image);if(imageUrl){image.src=imageUrl;image.alt=cake.name;image.loading='lazy';link.append(image);}const detail=node('div');detail.append(node('strong','',cake.name));detail.append(node('span','',new Intl.NumberFormat('vi-VN').format(Number(cake.price))+' đ'));detail.append(node('small','',cake.stock>0?'Còn '+cake.stock+' bánh':'Tạm hết hàng'));link.append(detail);bubble.append(link);}
  if((data.sources||[]).length){const sources=node('div','assistant-widget-sources');sources.append(node('strong','assistant-bubble-label','Nguồn thông tin'));for(const source of data.sources){const href=safeUrl(source.url);if(href){const link=node('a','',source.title);link.href=href;sources.append(link);}}bubble.append(sources);}
  if(data.checkedAt){const date=new Date(data.checkedAt);bubble.append(node('small','assistant-source-time','Đọc dữ liệu: '+(Number.isNaN(date.getTime())?data.checkedAt:date.toLocaleString('vi-VN'))));}
  if(data.handoff){const full=root.querySelector('.assistant-widget-links a');if(full){const link=node('a','assistant-widget-handoff','Gửi phiếu cho nhân viên →');link.href=full.href;bubble.append(link);}}
  log.append(bubble);scroll();
 };
 async function request(url,options={}){
  const controller=new AbortController(),timer=setTimeout(()=>controller.abort(),20000);
  try{const response=await fetch(url,{credentials:'same-origin',...options,signal:controller.signal});if(response.redirected||response.status===401||response.status===403)throw new Error('Phiên đã hết hoặc chưa có quyền. Hãy tải lại trang và đăng nhập.');if(!response.ok||!(response.headers.get('content-type')||'').includes('application/json'))throw new Error('Chưa gửi được câu hỏi. Hãy thử lại hoặc gửi phiếu hỗ trợ.');return await response.json();}finally{clearTimeout(timer);}
 }
 async function load(){if(!authenticated||loaded||busy)return;busy=true;if(submit)submit.disabled=true;try{const history=await request(root.dataset.historyUrl);for(const turn of history){question(turn.question);answer(turn.answer);}loaded=true;showError('');}catch(e){showError(e.name==='AbortError'?'Kết nối quá lâu. Hãy thử mở lại trợ lý.':e.message);}finally{busy=false;if(submit)submit.disabled=false;}}
 function setOpen(open){panel.hidden=!open;toggle.setAttribute('aria-expanded',String(open));toggle.setAttribute('aria-label',open?'Đóng trợ lý Bóng Bakery':'Mở trợ lý Bóng Bakery');if(open){load();if(input)input.focus();else close.focus();}else toggle.focus();}
 toggle.addEventListener('click',()=>setOpen(panel.hidden));close.addEventListener('click',()=>setOpen(false));
 root.addEventListener('keydown',event=>{if(event.key==='Escape'&&!panel.hidden){event.preventDefault();setOpen(false);}});
 async function send(text){const value=text.trim();if(!value||busy)return;if(value.length>600){showError('Câu hỏi tối đa 600 ký tự.');return;}busy=true;submit.disabled=true;showError('Trợ lý đang tra dữ liệu…');question(value);input.value='';try{const headers={'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8','Accept':'application/json'};headers[root.dataset.csrfHeader]=root.dataset.csrfToken;const data=await request(root.dataset.messageUrl,{method:'POST',headers,body:new URLSearchParams({question:value}).toString()});answer(data);showError('');loaded=true;}catch(e){showError(e.name==='AbortError'?'Kết nối quá lâu. Hãy thử lại hoặc gửi phiếu hỗ trợ.':e.message);input.value=value;}finally{busy=false;submit.disabled=false;input.focus();}}
 if(form)form.addEventListener('submit',event=>{event.preventDefault();send(input.value);});
 root.querySelectorAll('[data-assistant-prompt]').forEach(button=>button.addEventListener('click',()=>send(button.dataset.assistantPrompt)));
})();
