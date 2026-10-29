(() => {
 'use strict';
 document.querySelectorAll('[data-refund-fields]').forEach(fields=>{
  const form=fields.closest('form'),kind=form?.querySelector('select[name="kind"]');
  if(!kind)return;
  const update=()=>{const show=kind.value==='REFUND';fields.hidden=!show;fields.disabled=!show;};
  kind.addEventListener('change',update);update();
 });
})();
