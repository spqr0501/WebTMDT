const test=require('node:test');const assert=require('node:assert/strict');const fs=require('node:fs');const path=require('node:path');const vm=require('node:vm');
const source=name=>fs.readFileSync(path.join(__dirname,'../../main/resources/static/js',name),'utf8');
const settle=()=>new Promise(r=>setImmediate(r));
function element(extra={}){return Object.assign({events:{},value:'',disabled:false,hidden:false,dataset:{},children:[],attributes:{},addEventListener(n,fn){(this.events[n]||=[]).push(fn);},async fire(n,e={}){for(const fn of this.events[n]||[])await fn(e);},append(e){this.children.push(e);},replaceChildren(){this.children=[];},setAttribute(n,v){this.attributes[n]=v;},setCustomValidity(m){this.validity=m;}},extra);}
function accountFixture(value='20',max='5'){
 const input=element({value,min:'1',max}),holder=element(),form=element(),password=element(),confirmation=element();input.closest=s=>s==='form'?form:holder;
 const document={querySelectorAll:s=>s==='[data-stock-quantity]'?[input]:s==='form[data-account-confirm]'?[form]:[],getElementById:id=>id==='newPassword'?password:confirmation,createElement:()=>element()};form.dataset.accountConfirm='Xóa địa chỉ?';let approved=false;
 vm.runInNewContext(source('account.js'),{document,window:{confirm:()=>approved}});return{input,holder,form,password,confirmation,approve(){approved=true;}};
}
test('Số lượng vượt tồn bị giữ ở mức tối đa, có cảnh báo và không tạo lặp thông báo',async()=>{
 const f=accountFixture();await f.input.fire('input');assert.equal(f.input.value,'5');assert.equal(f.holder.children.length,1);assert.match(f.holder.children[0].textContent,/tối đa 5/);assert.equal(f.holder.children[0].attributes['role'],'status');f.input.value='999';await f.input.fire('change');assert.equal(f.input.value,'5');assert.equal(f.holder.children.length,1);
});
test('Giới hạn số lượng vẫn cho phép xóa ô nhập, chặn số âm và số thập phân',async()=>{
 const f=accountFixture('');await f.input.fire('input');assert.equal(f.input.value,'');f.input.value='-5';await f.input.fire('input');assert.equal(f.input.value,'1');f.input.value='2.8';await f.input.fire('input');assert.equal(f.input.value,'2');f.input.value='4';await f.input.fire('change');assert.equal(f.input.value,'4');
});
test('Xóa địa chỉ cần xác nhận và nhập lại mật khẩu mới cần khớp',async()=>{
 const f=accountFixture();let prevented=false;await f.form.fire('submit',{preventDefault(){prevented=true;}});assert.equal(prevented,true);f.approve();prevented=false;await f.form.fire('submit',{preventDefault(){prevented=true;}});assert.equal(prevented,false);f.password.value='Password123!';f.confirmation.value='OtherPassword';await f.confirmation.fire('input');assert.match(f.confirmation.validity,/chưa khớp/);f.confirmation.value=f.password.value;await f.confirmation.fire('input');assert.equal(f.confirmation.validity,'');
});
function deliveryFixture({tracking=null,fail=false,delayBegin=false}={}){
 const state=element(),info=element(),time=element(),open=element(),start=element(),stop=element({disabled:true}),gpsStatus=element();
 const box=element({dataset:{trackUrl:'/api/delivery/1'},querySelector:s=>({'[data-tracking-state]':state,'[data-driver-info]':info,'[data-tracking-time]':time,'[data-tracking-open]':open}[s])});
 const panel=element({dataset:{beginUrl:'/shipper/api/orders/1/begin',locationUrl:'/shipper/api/orders/1/location',stopUrl:'/shipper/api/orders/1/stop'},querySelector:s=>({'[data-gps-start]':start,'[data-gps-stop]':stop,'[data-gps-status]':gpsStatus}[s])});
 const doc=element({hidden:false,querySelectorAll:s=>s==='[data-delivery-tracking]'&&tracking?[box]:[],querySelector:s=>s==='[data-shipper-gps]'?(tracking?null:panel):s==='meta[name="_csrf"]'?{content:'csrf-token'}:s==='meta[name="_csrf_header"]'?{content:'X-CSRF-TOKEN'}:null});
 const window=element({isSecureContext:true});const calls=[];let success,error,watchCount=0,clears=0,resolveBegin;
 const fetch=async(url,options)=>{calls.push({url,options});if(fail)throw Error('Mất kết nối');if(url.endsWith('/begin')&&delayBegin)await new Promise(r=>{resolveBegin=r;});return{ok:true,headers:{get:()=> 'application/json'},json:async()=>url.endsWith('/begin')?{key:'session-key'}:tracking||{ok:true}};};
 const navigator={geolocation:{watchPosition(s,e){success=s;error=e;watchCount++;return 7;},clearWatch(){clears++;}}};
 vm.runInNewContext(source('delivery-map.js'),{document:doc,window,navigator,fetch,AbortController,Date,setTimeout,clearTimeout,setInterval:()=>3,clearInterval(){}});
 return {state,info,time,start,stop,gpsStatus,doc,window,calls,get watchCount(){return watchCount;},get clears(){return clears;},position(){success({timestamp:Date.now(),coords:{latitude:10.77,longitude:106.7,accuracy:7}});},denied(){error({code:1});},resolveBegin(){resolveBegin();}};
}
test('Theo dõi thể hiện vị trí đã cũ và tên shipper dạng chữ an toàn',async()=>{
 const f=deliveryFixture({tracking:{driverName:'<img onerror=alert(1)>',driverPhone:'0901112233',vehicle:'Xe máy',status:'Đang giao',sharing:true,updatedAt:new Date(Date.now()-100000).toISOString(),accuracy:10}});await settle();assert.match(f.state.textContent,/đã cũ/);assert.match(f.info.textContent,/<img/);assert.equal(f.info.innerHTML,undefined);assert.equal(f.calls[0].options.cache,'no-store');
});
test('Lỗi kết nối không khẳng định ghim cũ là vị trí hiện tại',async()=>{
 const f=deliveryFixture({tracking:{},fail:true});await settle();assert.match(f.state.textContent,/Mất kết nối/);assert.match(f.time.textContent,/Đừng xem ghim cũ/);
});
test('GPS chỉ gửi sau khi bật, có CSRF và phiên riêng; dừng thì không gửi tiếp',async()=>{
 const f=deliveryFixture();assert.equal(f.calls.length,0);await f.start.fire('click');assert.equal(f.watchCount,1);f.position();await settle();const call=f.calls.find(c=>c.url.endsWith('/location'));assert.ok(call);const payload=JSON.parse(call.options.body);assert.equal(payload.trackingKey,'session-key');assert.equal(payload.latitude,10.77);assert.equal(call.options.headers['X-CSRF-TOKEN'],'csrf-token');await f.stop.fire('click');const count=f.calls.length;f.position();await settle();assert.equal(f.calls.length,count);assert.equal(f.clears,1);assert.equal(f.start.disabled,false);assert.match(f.gpsStatus.textContent,/ẩn vị trí/);
});
test('Ẩn trang sẽ dừng GPS và báo máy chủ bằng keepalive',async()=>{
 const f=deliveryFixture();await f.start.fire('click');f.doc.hidden=true;await f.doc.fire('visibilitychange');await settle();assert.equal(f.clears,1);assert.ok(f.calls.find(c=>c.url.endsWith('/stop')&&c.options.keepalive));
});
test('Dừng trong lúc mở phiên không khởi động GPS khi phản hồi đến trễ',async()=>{
 const f=deliveryFixture({delayBegin:true});const pending=f.start.fire('click');await settle();await f.stop.fire('click');f.resolveBegin();await pending;assert.equal(f.watchCount,0);assert.equal(f.calls.filter(c=>c.url.endsWith('/stop')).length,2);
});
test('Từ chối quyền GPS dừng phiên và hiển thị nguyên nhân',async()=>{
 const f=deliveryFixture();await f.start.fire('click');f.denied();await settle();assert.match(f.gpsStatus.textContent,/chưa cho phép GPS/);assert.equal(f.clears,1);assert.ok(f.calls.some(c=>c.url.endsWith('/stop')));
});
