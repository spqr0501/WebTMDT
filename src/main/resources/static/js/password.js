(() => {
 'use strict';
 document.querySelectorAll('[data-password-toggle]').forEach(button => {
  const input=document.getElementById(button.getAttribute('aria-controls'));
  if(!input)return;
  button.addEventListener('click',()=>{
   const visible=input.type==='password';
   input.type=visible?'text':'password';
   button.classList.toggle('is-visible',visible);
   button.setAttribute('aria-pressed',String(visible));
   button.setAttribute('aria-label',visible?'Ẩn mật khẩu':'Hiện mật khẩu');
   button.title=visible?'Ẩn mật khẩu':'Hiện mật khẩu';
  });
 });
 const password=document.getElementById('registerPassword');
 const confirmation=document.getElementById('confirmPassword');
 if(password&&confirmation){
  const validate=()=>confirmation.setCustomValidity(confirmation.value&&confirmation.value!==password.value?'Mật khẩu nhập lại chưa khớp.':'');
  password.addEventListener('input',validate);
  confirmation.addEventListener('input',validate);
 }
})();
