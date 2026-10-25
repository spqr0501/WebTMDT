const test=require('node:test');const assert=require('node:assert/strict');const fs=require('node:fs');const path=require('node:path');const vm=require('node:vm');
const source=name=>fs.readFileSync(path.join(__dirname,'../../main/resources/static/js',name),'utf8');
const settle=()=>new Promise(resolve=>setImmediate(resolve));
function element(extra={}){return Object.assign({events:{},attributes:{},disabled:false,value:'',classes:new Set(),addEventListener(name,cb){this.events[name]=cb;},setAttribute(name,value){this.attributes[name]=value;},getAttribute(name){return this.attributes[name];},setCustomValidity(message){this.validityMessage=message;}},extra);}
function classes(el){return {toggle(name,value){if(value)el.classes.add(name);else el.classes.delete(name);},remove(name){el.classes.delete(name);}};}
test('Con mắt hiện/ẩn đúng ô mật khẩu và cập nhật nhãn truy cập',()=>{
 const p=element({type:'password'}),confirm=element({type:'password'}),a=element(),b=element();a.attributes['aria-controls']='registerPassword';b.attributes['aria-controls']='confirmPassword';a.classList=classes(a);b.classList=classes(b);
 vm.runInNewContext(source('password.js'),{document:{querySelectorAll:()=>[a,b],getElementById:id=>id==='registerPassword'?p:confirm}});a.events.click();assert.equal(p.type,'text');assert.equal(confirm.type,'password');assert.equal(a.attributes['aria-pressed'],'true');assert.equal(a.attributes['aria-label'],'Ẩn mật khẩu');a.events.click();assert.equal(p.type,'password');assert.equal(a.attributes['aria-label'],'Hiện mật khẩu');b.events.click();assert.equal(confirm.type,'text');
});
test('Nhập lại mật khẩu báo không khớp và xóa lỗi khi sửa đúng',()=>{
 const p=element({value:'Password123!'}),c=element({value:'Other123!'});vm.runInNewContext(source('password.js'),{document:{querySelectorAll:()=>[],getElementById:id=>id==='registerPassword'?p:c}});c.events.input();assert.ok(c.validityMessage.includes('chưa khớp'));c.value=p.value;c.events.input();assert.equal(c.validityMessage,'');p.value='NewPassword123!';p.events.input();assert.ok(c.validityMessage);c.value='';c.events.input();assert.equal(c.validityMessage,'');
});
function favoriteFixture({fail=false,back='product'}={}){
 const button=element(),status=element(),hidden=element({value:'true'}),otherButton=element(),otherHidden=element({value:'true'});button.classList=classes(button);status.classList=classes(status);otherButton.classList=classes(otherButton);
 const form=element({dataset:{stateUrl:'/favorites/1/state',productId:'1'}}),other=element({dataset:{productId:'1'}}),confirm=element();form.querySelector=s=>s==='button'?button:s==='[role="status"]'?status:s==='[name="favorite"]'?hidden:{value:back};other.querySelector=s=>s==='button'?otherButton:otherHidden;
 const calls=[];let reloaded=false,approved=false;const fetch=async(url,options)=>{calls.push({url,options});if(fail)throw Error('Mất kết nối');return {ok:true,redirected:false,headers:{get:()=> 'application/json'},json:async()=>({favorite:back!=='favorites',message:'Đã cập nhật yêu thích.'})};};
 vm.runInNewContext(source('favorites.js'),{document:{querySelectorAll:s=>s==='[data-favorite-form]'?[form,other]:[confirm]},window:{fetch,location:{reload(){reloaded=true;}},confirm(){return approved;}},fetch,FormData:class{constructor(f){this.form=f;}}});return {form,button,status,hidden,otherButton,otherHidden,confirm,calls,get reloaded(){return reloaded;},approve(){approved=true;}};
}
test('Nút tim lưu bằng POST và đồng bộ các nút của cùng bánh',async()=>{
 const f=favoriteFixture();let prevented=false;f.form.events.submit({preventDefault(){prevented=true;}});assert.equal(prevented,true);assert.equal(f.button.disabled,true);await settle();assert.equal(f.calls[0].options.method,'POST');assert.equal(f.calls[0].url,'/favorites/1/state');assert.equal(f.hidden.value,'false');assert.equal(f.otherHidden.value,'false');assert.equal(f.button.attributes['aria-pressed'],'true');assert.ok(f.otherButton.classes.has('is-favorite'));assert.equal(f.button.disabled,false);
});
test('Lỗi lưu yêu thích không đổi trạng thái tim và cho phép thử lại',async()=>{
 const f=favoriteFixture({fail:true});f.form.events.submit({preventDefault(){}});await settle();assert.equal(f.hidden.value,'true');assert.equal(f.button.disabled,false);assert.ok(f.status.textContent.includes('Chưa lưu được'));
});
test('Bỏ yêu thích trên danh sách tải lại và xóa phụ kiện có xác nhận',async()=>{
 const f=favoriteFixture({back:'favorites'});f.form.events.submit({preventDefault(){}});await settle();assert.equal(f.reloaded,true);let prevented=false;f.confirm.events.submit({preventDefault(){prevented=true;}});assert.equal(prevented,true);f.approve();prevented=false;f.confirm.events.submit({preventDefault(){prevented=true;}});assert.equal(prevented,false);
});
