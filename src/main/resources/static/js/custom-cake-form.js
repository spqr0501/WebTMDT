(() => {
 'use strict';
 const money=document.querySelector('[data-money-vnd]');
 function moneyValidity(){
  const raw=money.value.replaceAll('.','');
  const good=/^[0-9]+$/.test(raw)&&Number(raw)>=10000&&Number(raw)<=999999999;
  money.setCustomValidity(good?'':'Nhập ngân sách từ 10.000 đến 999.999.999 đ.');
 }
 function formatMoney(){
  const value=money.value;
  if(!/^[0-9.]*$/.test(value)){money.setCustomValidity('Chỉ nhập số tiền nguyên đồng, ví dụ 100.000.');return;}
  const pos=money.selectionStart??value.length,digitsBefore=value.slice(0,pos).replaceAll('.','').length;
  const digits=value.replaceAll('.','').replace(/^0+(?=\d)/,'');
  money.value=digits.replace(/\B(?=(\d{3})+(?!\d))/g,'.');
  let cursor=0,count=0;while(cursor<money.value.length&&count<digitsBefore){if(/\d/.test(money.value[cursor]))count++;cursor++;}
  money.setSelectionRange(cursor,cursor);moneyValidity();
 }
 if(money){formatMoney();money.addEventListener('input',formatMoney);}
 document.querySelectorAll('[data-vn-phone]').forEach(phone=>{const check=()=>{let s=phone.value.trim().replace(/[\s().-]/g,'');if(s.startsWith('+84'))s='0'+s.slice(3);else if(/^84\d{9}$/.test(s))s='0'+s.slice(2);phone.setCustomValidity((phone.hasAttribute('data-phone-optional')&&s==='')||/^0[35789]\d{8}$/.test(s)?'':'Nhập số di động Việt Nam 10 số, ví dụ 0902592231.');return s;};phone.addEventListener('input',check);phone.addEventListener('blur',()=>{phone.value=check();});check();});
 const date=document.querySelector('[data-vn-date]'),picker=document.querySelector('[data-date-picker]');
 if(date&&picker){
  function parseVi(value){const m=/^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value);if(!m)return '';const d=new Date(Date.UTC(+m[3],+m[2]-1,+m[1]));return d.getUTCFullYear()===+m[3]&&d.getUTCMonth()===+m[2]-1&&d.getUTCDate()===+m[1]?`${m[3]}-${m[2]}-${m[1]}`:'';}
  const toVi=iso=>{const [y,m,d]=iso.split('-');return `${d}/${m}/${y}`;};
  const isoDay=(year,month,day)=>`${year}-${String(month+1).padStart(2,'0')}-${String(day).padStart(2,'0')}`;
  const calendar=document.createElement('div');calendar.className='vi-calendar mb-3';calendar.hidden=true;calendar.setAttribute('role','group');calendar.setAttribute('aria-label','Lịch chọn ngày theo ngày tháng năm');picker.after(calendar);
  let month=(parseVi(date.value)||date.dataset.min).slice(0,7);
  const close=()=>{calendar.hidden=true;picker.setAttribute('aria-expanded','false');};
  function render(){
   calendar.replaceChildren();const [year,monthNumber]=month.split('-').map(Number),monthIndex=monthNumber-1;
   const controls=document.createElement('div');controls.className='vi-calendar-controls';
   const previous=document.createElement('button'),next=document.createElement('button'),caption=document.createElement('strong');
   previous.type=next.type='button';previous.textContent='←';next.textContent='→';previous.setAttribute('aria-label','Tháng trước');next.setAttribute('aria-label','Tháng sau');caption.textContent=`Tháng ${monthNumber} / ${year}`;caption.setAttribute('aria-live','polite');
   const shift=n=>{const d=new Date(Date.UTC(year,monthIndex+n,1));return isoDay(d.getUTCFullYear(),d.getUTCMonth(),1).slice(0,7);};
   previous.disabled=shift(-1)<date.dataset.min.slice(0,7);next.disabled=shift(1)>date.dataset.max.slice(0,7);
   previous.addEventListener('click',()=>{month=shift(-1);render();});next.addEventListener('click',()=>{month=shift(1);render();});controls.append(previous,caption,next);calendar.append(controls);
   const grid=document.createElement('div');grid.className='vi-calendar-grid';
   ['T2','T3','T4','T5','T6','T7','CN'].forEach(text=>{const cell=document.createElement('span');cell.className='vi-weekday';cell.textContent=text;grid.append(cell);});
   const offset=(new Date(Date.UTC(year,monthIndex,1)).getUTCDay()+6)%7;
   for(let i=0;i<offset;i++){const empty=document.createElement('span');empty.setAttribute('aria-hidden','true');grid.append(empty);}
   const count=new Date(Date.UTC(year,monthIndex+1,0)).getUTCDate();
   for(let day=1;day<=count;day++){const iso=isoDay(year,monthIndex,day),button=document.createElement('button');button.type='button';button.textContent=String(day);button.disabled=iso<date.dataset.min||iso>date.dataset.max;button.setAttribute('aria-label','Chọn ngày '+toVi(iso));button.setAttribute('aria-pressed',String(parseVi(date.value)===iso));if(parseVi(date.value)===iso)button.className='is-selected';button.addEventListener('click',()=>{date.value=toVi(iso);date.dispatchEvent(new Event('input',{bubbles:true}));date.dispatchEvent(new Event('change',{bubbles:true}));close();picker.focus();});grid.append(button);}
   calendar.append(grid);const hint=document.createElement('p');hint.className='small muted mt-2 mb-0';hint.textContent=`Từ ${toVi(date.dataset.min)} đến ${toVi(date.dataset.max)}`;calendar.append(hint);
  }
  function checkDate(){const iso=parseVi(date.value);date.setCustomValidity(iso&&iso>=date.dataset.min&&iso<=date.dataset.max?'':(date.id==='deliveryDate'?'Nhập ngày/tháng/năm hợp lệ: từ ngày mai đến 30 ngày, hoặc bật đặt trước để chọn đến 90 ngày (giờ Việt Nam).':'Nhập ngày/tháng/năm hợp lệ, đặt trước từ 3 đến 90 ngày (giờ Việt Nam).'));picker.textContent=iso?'Chọn ngày: '+toVi(iso):'Chọn ngày (ngày/tháng/năm)';if(!calendar.hidden)render();return iso;}
  picker.addEventListener('click',()=>{if(!calendar.hidden){close();return;}month=(parseVi(date.value)||date.dataset.min).slice(0,7);if(month<date.dataset.min.slice(0,7)||month>date.dataset.max.slice(0,7))month=date.dataset.min.slice(0,7);calendar.hidden=false;picker.setAttribute('aria-expanded','true');render();});
  calendar.addEventListener('keydown',e=>{if(e.key==='Escape'){close();picker.focus();}});
  document.addEventListener('click',e=>{if(!calendar.contains(e.target)&&e.target!==picker)close();});
  date.addEventListener('input',checkDate);
  const preorder=document.getElementById('preorder');
  if(preorder){const updateRange=()=>{date.dataset.max=preorder.checked?date.dataset.preorderMax:date.dataset.normalMax;if(month>date.dataset.max.slice(0,7))month=date.dataset.min.slice(0,7);checkDate();};preorder.addEventListener('change',updateRange);updateRange();}else checkDate();
 }
 const address=document.querySelector('[data-address-auto-map] [data-address-text]'),fulfillment=document.querySelector('[data-fulfillment]');
 if(address&&fulfillment){const update=()=>{address.required=fulfillment.value==='DELIVERY';};fulfillment.addEventListener('change',update);update();}
})();
