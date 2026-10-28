(() => {
 'use strict';
 document.querySelectorAll('form[data-account-confirm]').forEach(form=>form.addEventListener('submit',event=>{if(!window.confirm(form.dataset.accountConfirm))event.preventDefault();}));
 const password=document.getElementById('newPassword'),confirmation=document.getElementById('confirmNewPassword');
 if(password&&confirmation){const check=()=>confirmation.setCustomValidity(confirmation.value&&confirmation.value!==password.value?'Mật khẩu nhập lại chưa khớp.':'');password.addEventListener('input',check);confirmation.addEventListener('input',check);}
 document.querySelectorAll('[data-stock-quantity]').forEach(input=>{
  let warning;const clamp=()=>{
   if(input.value.trim()===''||!Number.isFinite(Number(input.value)))return false;
   const min=Number(input.min||0),max=Number(input.max),value=Number(input.value);if(!Number.isFinite(max)||max<min)return false;
   let message='';if(value>max){input.value=String(max);message=`Chỉ có thể chọn tối đa ${max} cho lựa chọn này. Số lượng đã được giữ ở ${max}.`;}else if(value<min){input.value=String(min);message=`Số lượng tối thiểu là ${min}.`;}else if(!Number.isInteger(value)){input.value=String(Math.floor(value));message='Số lượng cần là số nguyên.';}
   if(message){if(!warning){warning=document.createElement('span');warning.className='stock-quantity-warning';warning.setAttribute('role','status');warning.setAttribute('aria-live','polite');const holder=input.closest('[data-quantity-wrap]')||input.parentElement;holder.append(warning);}warning.textContent=message;warning.hidden=false;return true;}return false;
  };
  input.addEventListener('input',clamp);input.addEventListener('change',clamp);input.closest('form')?.addEventListener('submit',clamp);
 });
})();
