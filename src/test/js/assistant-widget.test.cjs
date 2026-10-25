const test=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const vm=require('node:vm');
const source=fs.readFileSync(path.join(__dirname,'../../main/resources/static/js/assistant-widget.js'),'utf8');
class Element {
 constructor(tag='div'){this.tagName=tag;this.children=[];this.events={};this.attributes={};this.dataset={};this.hidden=false;this.textContent='';this.value='';this.scrollHeight=100;this.focused=false;}
 append(...items){this.children.push(...items);}
 addEventListener(name,handler){this.events[name]=handler;}
 setAttribute(name,value){this.attributes[name]=value;}
 focus(){this.focused=true;}
}
const settle=()=>new Promise(resolve=>setImmediate(resolve));
function fixture({authenticated=true,handler,absent=false}={}){
 const root=new Element(),toggle=new Element('button'),panel=new Element(),close=new Element('button'),form=new Element('form'),input=new Element('input'),submit=new Element('button'),log=new Element(),error=new Element(),support=new Element('a'),prompt=new Element('button');
 panel.hidden=true;support.href='/support/new';prompt.dataset.assistantPrompt='Gợi ý bánh kem dưới 500k';root.dataset={authenticated:String(authenticated),historyUrl:'/assistant/history',messageUrl:'/assistant/message',csrfHeader:'X-CSRF-TOKEN',csrfToken:'test-csrf'};
 const selectors={'[data-assistant-toggle]':toggle,'#bakeryAssistantPanel':panel,'[data-assistant-close]':close,'[data-assistant-form]':authenticated?form:null,'[data-assistant-input]':authenticated?input:null,'[data-assistant-submit]':authenticated?submit:null,'[data-assistant-messages]':log,'[data-assistant-error]':error,'.assistant-widget-links a':support};
 root.querySelector=selector=>selectors[selector]||null;root.querySelectorAll=()=>authenticated?[prompt]:[];
 const calls=[];const response=data=>({ok:true,status:200,redirected:false,headers:{get:()=> 'application/json'},json:async()=>data});
 const context=vm.createContext({document:{querySelector:()=>absent?null:root,createElement:tag=>new Element(tag)},window:{location:{href:'http://localhost:8080/products',origin:'http://localhost:8080'}},URL,URLSearchParams,AbortController,setTimeout,clearTimeout,fetch:async(url,options)=>{calls.push({url,options});return handler?handler(url,options,response):response(url.includes('history')?[]:{text:'Cửa hàng có bánh phù hợp.',sources:[{title:'Chính sách',url:'/policies#delivery'}],cakes:[],checkedAt:'2026-10-25T12:00:00',handoff:true});}});
 vm.runInContext(source,context);return {root,toggle,panel,close,form,input,submit,log,error,prompt,calls};
}
function allElements(el){return [el,...el.children.flatMap(allElements)];}
test('Nút tròn mở/đóng, Escape và trả lại vị trí nhập bàn phím',async()=>{
 const f=fixture();f.toggle.events.click();assert.equal(f.panel.hidden,false);assert.equal(f.toggle.attributes['aria-expanded'],'true');assert.equal(f.input.focused,true);await settle();assert.equal(f.calls.length,1);let prevented=false;f.root.events.keydown({key:'Escape',preventDefault(){prevented=true;}});assert.equal(prevented,true);assert.equal(f.panel.hidden,true);assert.equal(f.toggle.focused,true);f.toggle.events.click();await settle();assert.equal(f.calls.length,1);f.close.events.click();assert.equal(f.panel.hidden,true);
});
test('Khách chưa đăng nhập có thể mở khung, không gọi hội thoại riêng',()=>{
 const f=fixture({authenticated:false});f.toggle.events.click();assert.equal(f.panel.hidden,false);assert.equal(f.close.focused,true);assert.equal(f.calls.length,0);
});
test('Gửi câu hỏi có CSRF, đọc nguồn, ảnh/giá bánh và chống chèn HTML',async()=>{
 const f=fixture({handler:(url,options,response)=>response(url.includes('history')?[]:{text:'<script>không thực thi</script>',sources:[{title:'Bánh',url:'/products/1'},{title:'Không được dùng',url:'javascript:alert(1)'}],cakes:[{name:'Bánh kem',image:'/images/cake.svg',url:'/products/1',price:320000,stock:2}],checkedAt:'2026-10-25T12:00:00',handoff:true})});f.toggle.events.click();await settle();f.input.value='Bánh kem dưới 400k';f.form.events.submit({preventDefault(){}});await settle();assert.equal(f.calls[1].options.headers['X-CSRF-TOKEN'],'test-csrf');assert.equal(new URLSearchParams(f.calls[1].options.body).get('question'),'Bánh kem dưới 400k');assert.equal(f.submit.disabled,false);const elements=allElements(f.log);assert.ok(elements.some(e=>e.textContent==='<script>không thực thi</script>'));assert.ok(!elements.some(e=>e.tagName==='script'));assert.ok(elements.some(e=>e.href==='/products/1'));assert.ok(!elements.some(e=>(e.href||'').startsWith('javascript:')));assert.ok(elements.some(e=>e.tagName==='img'&&e.src==='/images/cake.svg'));assert.ok(elements.some(e=>e.href==='/support/new'));
});
test('Hết phiên hoặc lỗi mạng hiển thị lỗi và giữ câu hỏi để gửi lại',async()=>{
 const f=fixture({handler:(url,options,response)=>url.includes('history')?response([]):Promise.reject(new Error('Mạng gián đoạn'))});f.toggle.events.click();await settle();f.input.value='Tôi muốn hỏi bảo quản bánh';f.form.events.submit({preventDefault(){}});await settle();assert.equal(f.error.hidden,false);assert.equal(f.input.value,'Tôi muốn hỏi bảo quản bánh');assert.equal(f.submit.disabled,false);
 const g=fixture({handler:()=>({ok:true,status:200,redirected:true,headers:{get:()=> 'text/html'}})});g.toggle.events.click();await settle();assert.ok(g.error.textContent.includes('đăng nhập'));assert.equal(g.submit.disabled,false);
});
test('Không gửi trùng khi đang chờ trả lời; câu hỏi nhanh dùng cùng luồng gửi',async()=>{
 let resolveMessage;const f=fixture({handler:(url,options,response)=>url.includes('history')?response([]):new Promise(resolve=>{resolveMessage=()=>resolve(response({text:'Đã tìm được bánh',sources:[],cakes:[],handoff:false}));})});f.toggle.events.click();await settle();f.prompt.events.click();assert.equal(f.submit.disabled,true);f.input.value='Câu thứ hai';f.form.events.submit({preventDefault(){}});assert.equal(f.calls.length,2);resolveMessage();await settle();assert.equal(f.submit.disabled,false);
});
test('Trang không có nút trợ lý vẫn chạy bình thường',()=>assert.doesNotThrow(()=>fixture({absent:true})));
