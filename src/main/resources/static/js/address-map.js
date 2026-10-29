(() => {
 'use strict';
 const mapConfig=()=>({url:document.querySelector('meta[name="map-tiles"]')?.content||'https://tile.openstreetmap.org/{z}/{x}/{y}.png',attribution:'&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">OpenStreetMap</a> contributors'});
 window.bakeryMapConfig=mapConfig;
 document.querySelectorAll('[data-address-picker]').forEach(box=>{
  const entry=box.closest('[data-address-entry]'),address=entry?.querySelector('[data-address-text]');if(!address)return;
  const lat=box.querySelector('[data-latitude]'),lng=box.querySelector('[data-longitude]'),status=box.querySelector('[data-map-status]'),tools=box.querySelector('[data-map-tools]');
  let map,marker,seq=0,pending=false,autoTimer;
  const cancelAuto=()=>clearTimeout(autoTimer);
  const valid=()=>lat.value!==''&&lng.value!==''&&Number.isFinite(Number(lat.value))&&Number.isFinite(Number(lng.value));
  const say=t=>{status.textContent=t;};
  function open(){tools.hidden=false;if(!window.L){say('Chưa tải được bản đồ. Bạn vẫn có thể nhập địa chỉ bằng tay.');return false;}if(!map){map=L.map(box.querySelector('[data-map-canvas]')).setView(valid()?[Number(lat.value),Number(lng.value)]:[10.7769,106.7009],valid()?16:12);const c=mapConfig();L.tileLayer(c.url,{attribution:c.attribution,maxZoom:19}).addTo(map);map.on('click',e=>{cancelAuto();seq++;pin(e.latlng.lat,e.latlng.lng);say('Đã chọn ghim. Bấm Điền địa chỉ tại ghim hoặc nhập địa chỉ bên dưới.');});if(valid())pin(Number(lat.value),Number(lng.value));}requestAnimationFrame(()=>map.invalidateSize());return true;}
  function pin(a,b){lat.value=Number(a).toFixed(6);lng.value=Number(b).toFixed(6);if(map){if(marker)marker.setLatLng([a,b]);else{marker=L.marker([a,b],{draggable:true}).addTo(map);marker.on('dragend',()=>{cancelAuto();seq++;const p=marker.getLatLng();pin(p.lat,p.lng);say('Ghim đã di chuyển. Bấm Điền địa chỉ tại ghim để cập nhật.');});}map.setView([a,b],Math.max(15,map.getZoom()));}}
  async function fetchPlaces(url){const controller=new AbortController(),timer=setTimeout(()=>controller.abort(),10000);try{const r=await fetch(url,{headers:{Accept:'application/json'},cache:'no-store',signal:controller.signal});if(!r.headers.get('content-type')?.includes('application/json'))throw Error('Phiên đăng nhập đã hết. Hãy đăng nhập lại.');const data=await r.json();if(!r.ok)throw Error(data.message||'Không tra được địa chỉ.');return data;}finally{clearTimeout(timer);}}
  async function reverse(){if(!valid()){say('Hãy chọn vị trí trên bản đồ trước.');return;}cancelAuto();const n=++seq;say('Đang lấy địa chỉ tại ghim…');try{const result=await fetchPlaces(`${box.dataset.reverseUrl}?lat=${lat.value}&lng=${lng.value}`);if(n!==seq)return;if(!result.length){say('Chưa tìm thấy địa chỉ tại ghim. Hãy nhập số nhà và tên đường bằng tay.');return;}address.value=result[0].address;address.dispatchEvent(new Event('change',{bubbles:true}));say('Đã điền địa chỉ. Vui lòng kiểm tra và bổ sung số nhà, tầng hoặc hẻm.');}catch(e){if(n===seq)say(e.name==='AbortError'?'Tra địa chỉ quá lâu. Bạn có thể nhập bằng tay.':e.message);}}
  box.querySelector('[data-open-map]').addEventListener('click',open);
  box.querySelector('[data-reverse]').addEventListener('click',reverse);
  box.querySelector('[data-clear-pin]').addEventListener('click',()=>{cancelAuto();seq++;lat.value='';lng.value='';if(marker){marker.remove();marker=null;}say('Đã bỏ ghim. Địa chỉ chữ vẫn được giữ.');});
  box.querySelector('[data-locate]').addEventListener('click',()=>{if(pending)return;if(!navigator.geolocation){say('Thiết bị chưa hỗ trợ GPS. Hãy chọn trên bản đồ.');return;}pending=true;cancelAuto();const n=++seq;say('Đang xin quyền và lấy vị trí hiện tại…');navigator.geolocation.getCurrentPosition(p=>{pending=false;if(n!==seq)return;open();pin(p.coords.latitude,p.coords.longitude);say(`Đã lấy vị trí, sai số khoảng ${Math.round(p.coords.accuracy)} m. Đang tra địa chỉ…`);reverse();},e=>{pending=false;if(n!==seq)return;say(e.code===1?'Bạn chưa cho phép vị trí. Hãy chọn ghim hoặc nhập bằng tay.':'Chưa lấy được GPS. Bật vị trí hoặc chọn ghim trên bản đồ.');},{enableHighAccuracy:true,timeout:15000,maximumAge:0});});
  const search=async()=>{const q=box.querySelector('[data-map-query]').value.trim();if(q.length<3){say('Nhập địa chỉ từ 3 ký tự rồi bấm Tìm.');return;}cancelAuto();const n=++seq,results=box.querySelector('[data-map-results]');results.replaceChildren();say('Đang tìm địa chỉ…');try{const places=await fetchPlaces(`${box.dataset.searchUrl}?q=${encodeURIComponent(q)}`);if(n!==seq)return;places.forEach(p=>{const button=document.createElement('button');button.type='button';button.className='list-group-item list-group-item-action';button.textContent=p.address;button.addEventListener('click',()=>{cancelAuto();seq++;pin(p.latitude,p.longitude);address.value=p.address;address.dispatchEvent(new Event('change',{bubbles:true}));results.replaceChildren();say('Đã chọn địa chỉ. Kiểm tra số nhà và vị trí ghim trước khi lưu.');});results.append(button);});say(places.length?'Chọn một kết quả bên dưới.':'Chưa thấy địa chỉ. Hãy nhập rõ tỉnh/thành phố hoặc chọn ghim.');}catch(e){if(n===seq)say(e.message);}};
  entry.closest('form')?.querySelector('[data-fulfillment]')?.addEventListener('change',()=>{cancelAuto();seq++;});
  if(entry.hasAttribute('data-address-auto-map'))address.addEventListener('input',()=>{
   cancelAuto();const n=++seq;lat.value='';lng.value='';if(marker){marker.remove();marker=null;}
   box.querySelector('[data-map-results]').replaceChildren();
   const q=address.value.trim();if(q.length<12){say('Nhập đủ số nhà, đường và tỉnh/thành phố để tự ghim bản đồ.');return;}
   say('Sẽ tìm vị trí khi bạn ngừng nhập…');
   autoTimer=setTimeout(async()=>{
    try{const places=await fetchPlaces(`${box.dataset.searchUrl}?q=${encodeURIComponent(q.slice(0,200))}`);if(n!==seq)return;
     if(!places.length){say('Chưa tìm thấy vị trí. Bổ sung tỉnh/thành phố hoặc tự chọn ghim.');return;}
     open();pin(places[0].latitude,places[0].longitude);
     const results=box.querySelector('[data-map-results]');
     places.forEach(p=>{const button=document.createElement('button');button.type='button';button.className='list-group-item list-group-item-action';button.textContent=p.address;button.addEventListener('click',()=>{cancelAuto();seq++;pin(p.latitude,p.longitude);results.replaceChildren();say('Đã chọn vị trí. Địa chỉ bạn nhập được giữ nguyên; kiểm tra số nhà và ghim trước khi gửi.');});results.append(button);});
     say('Đã ghim kết quả gần nhất: '+places[0].address+'. Hãy kiểm tra, chọn kết quả khác hoặc kéo ghim nếu cần.');
    }catch(e){if(n===seq)say(e.name==='AbortError'?'Tra địa chỉ quá lâu; bạn có thể chọn ghim hoặc nhập bằng tay.':e.message);}
   },1600);
  });
  box.querySelector('[data-map-search]').addEventListener('click',search);box.querySelector('[data-map-query]').addEventListener('keydown',e=>{if(e.key==='Enter'){e.preventDefault();search();}});
 });
 const checkbox=document.querySelector('[data-use-new-address]');if(checkbox){const update=()=>{const group=document.querySelector('[data-inline-address]'),delivery=document.querySelector('[data-fulfillment]').value==='DELIVERY';group.hidden=!checkbox.checked||!delivery;group.querySelectorAll('input,textarea,select').forEach(f=>{f.disabled=!checkbox.checked||!delivery;});document.querySelectorAll('input[name="addressId"]').forEach(f=>{f.disabled=checkbox.checked||!delivery;});};checkbox.addEventListener('change',update);document.querySelector('[data-fulfillment]')?.addEventListener('change',()=>{if(document.querySelector('[data-fulfillment]').value==='DELIVERY')update();});update();}
})();
