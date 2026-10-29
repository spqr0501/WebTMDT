package com.example.demo.controller;
import com.example.demo.service.*;
import com.example.demo.dto.CheckoutForm;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.UUID;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
@Controller
public class OrderController {
 @InitBinder("form") public void formBinding(org.springframework.web.bind.WebDataBinder binder){
  binder.registerCustomEditor(java.time.LocalDate.class,"deliveryDate",new java.beans.PropertyEditorSupport(){
   private final java.time.format.DateTimeFormatter vi=java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
   public void setAsText(String value){try{setValue(java.time.LocalDate.parse(value,vi));}catch(java.time.format.DateTimeParseException e){setValue(java.time.LocalDate.parse(value,java.time.format.DateTimeFormatter.ISO_LOCAL_DATE));}}
   public String getAsText(){return getValue()==null?"":vi.format((java.time.LocalDate)getValue());}
  });
 }

 @org.springframework.beans.factory.annotation.Autowired private com.example.demo.repository.StripePaymentRepository stripeAttempts;
 private final PickupService pickups;private final PaymentService payments;private final AccessoryService accessories;private final OrderService orders;private final CartService cart;private final AccountService accounts;private final VoucherService vouchers;
 @Value("${app.bank.instructions}") private String bank;
 public OrderController(OrderService orders,CartService cart,AccountService accounts,VoucherService vouchers,AccessoryService accessories,PaymentService payments,PickupService pickups){this.pickups=pickups;this.payments=payments;this.accessories=accessories;this.orders=orders;this.cart=cart;this.accounts=accounts;this.vouchers=vouchers;}
 private void model(Principal p,HttpSession s,Model m){
  String stockWarning=cart.takeWarning(s);if(stockWarning!=null)m.addAttribute("warning",stockWarning);
  CheckoutForm f=(CheckoutForm)m.getAttribute("form");if(f.getAccessories()==null)f.setAccessories(new java.util.TreeMap<>());BigDecimal cakeSubtotal=cart.subtotal(s),extras=BigDecimal.ZERO;
  try{extras=accessories.total(f.getAccessories());}catch(BusinessException e){m.addAttribute("accessoryError",e.getMessage());}
  BigDecimal total=cakeSubtotal.add(extras).subtract((BigDecimal)(m.containsAttribute("discount")?m.getAttribute("discount"):BigDecimal.ZERO)).add(f.getFulfillmentMethod()==com.example.demo.entity.FulfillmentMethod.PICKUP?BigDecimal.ZERO:OrderService.shipping(cakeSubtotal));
  m.addAttribute("upfrontDue",f.getFulfillmentMethod()==com.example.demo.entity.FulfillmentMethod.PICKUP?(f.getPaymentPlan()==com.example.demo.entity.PaymentPlan.FULL?total:total.multiply(new BigDecimal("0.5")).setScale(0,java.math.RoundingMode.CEILING)):((f.getPaymentMethod()==com.example.demo.entity.PaymentMethod.BANK_TRANSFER||f.getPaymentMethod()==com.example.demo.entity.PaymentMethod.STRIPE)?total:BigDecimal.ZERO));
  m.addAttribute("cakeSubtotal",cakeSubtotal);m.addAttribute("accessoryTotal",extras);m.addAttribute("accessoryOptions",accessories.available());
  m.addAttribute("minDate",BakeryClock.today().plusDays(1));m.addAttribute("maxDate",BakeryClock.today().plusDays(f.getPreorder()?90:30));m.addAttribute("addresses",accounts.addresses(p.getName()));m.addAttribute("lines",cart.lines(s));m.addAttribute("subtotal",cakeSubtotal.add(extras));m.addAttribute("shipping",f.getFulfillmentMethod()==com.example.demo.entity.FulfillmentMethod.PICKUP?BigDecimal.ZERO:OrderService.shipping(cakeSubtotal));m.addAttribute("bank",bank);if(!m.containsAttribute("discount"))m.addAttribute("discount",BigDecimal.ZERO);
 }
 @GetMapping("/checkout") public String checkout(Principal p,HttpSession s,Model m){cart.reconcile(s);if(cart.snapshot(s).isEmpty()||cart.lines(s).stream().anyMatch(l->!l.available()))return "redirect:/cart";if(s.getAttribute("checkoutToken")==null)s.setAttribute("checkoutToken",UUID.randomUUID().toString());CheckoutForm f=new CheckoutForm();f.setPickupRecipient(accounts.current(p.getName()).getName());f.setPickupPhone(accounts.current(p.getName()).getPhone());f.getNewAddress().setRecipient(accounts.current(p.getName()).getName());f.getNewAddress().setPhone(accounts.current(p.getName()).getPhone());
  Object saved=s.getAttribute("reorderAccessories");if(saved instanceof java.util.Map<?,?> values){java.util.Map<Long,Integer> selected=new java.util.TreeMap<>();values.forEach((id,qty)->{if(id instanceof Long key&&qty instanceof Integer count)selected.put(key,count);});f.setAccessories(selected);}
f.setDeliveryDate(BakeryClock.today().plusDays(1));var preference=accounts.current(p.getName());f.setFulfillmentMethod(preference.getLastFulfillmentMethod()==null?com.example.demo.entity.FulfillmentMethod.DELIVERY:preference.getLastFulfillmentMethod());f.setDeliverySlot(preference.getLastDeliverySlot()==null?"09:00-12:00":preference.getLastDeliverySlot());var savedAddresses=accounts.addresses(p.getName());if(!savedAddresses.isEmpty())f.setAddressId(savedAddresses.stream().filter(com.example.demo.entity.Address::getDefaultAddress).findFirst().orElse(savedAddresses.get(0)).getId());else f.setUseNewAddress(true);f.setToken((String)s.getAttribute("checkoutToken"));m.addAttribute("form",f);model(p,s,m);return "shop/checkout";}
 @PostMapping("/checkout") public String place(Principal p,HttpSession s,@Valid @ModelAttribute("form") CheckoutForm f,BindingResult errors,@RequestParam(defaultValue="place") String action,Model m){
  if(f.getToken()==null||!f.getToken().equals(s.getAttribute("checkoutToken"))){throw new BusinessException("Phiên thanh toán đã kết thúc. Hãy kiểm tra lịch sử đơn hàng trước khi đặt lại.");}
  if(errors.hasErrors()){cart.reconcile(s);model(p,s,m);return "shop/checkout";}
  try{
   OrderService.validateDelivery(f);accessories.total(f.getAccessories());
   if("preview".equals(action)){m.addAttribute("discount",vouchers.preview(f.getVoucherCode(),accounts.current(p.getName()).getId(),cart.subtotal(s)));m.addAttribute("success","Đã tính lại phụ kiện, phí giao hàng và ưu đãi. Giá được kiểm tra lại khi đặt đơn.");model(p,s,m);return "shop/checkout";}
   var purchased=cart.detailed(s);var order=orders.checkoutDetailed(p.getName(),purchased,f);synchronized(s){if(f.getToken().equals(s.getAttribute("checkoutToken"))){cart.removePurchased(s,purchased);s.removeAttribute("reorderAccessories");}}return "redirect:/orders/"+order.getId();
  }catch(BusinessException e){cart.reconcile(s);errors.reject("checkout",e.getMessage());model(p,s,m);return "shop/checkout";}
 }
 @GetMapping("/orders") public String history(Principal p,@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("page",orders.history(p.getName(),page));return "account/orders";}
 @GetMapping("/orders/{id}") public String detail(Principal p,@PathVariable Long id,Model m){var order=orders.owned(p.getName(),id);m.addAttribute("order",order);m.addAttribute("stripeHistory",stripeAttempts.findByOrderIdOrderByCreatedAtDesc(id));if(order.getFulfillmentMethod()==com.example.demo.entity.FulfillmentMethod.PICKUP)m.addAttribute("pickupCode",pickups.orderCode(order));m.addAttribute("paymentRecords",payments.orderHistory(id));m.addAttribute("bank",bank);return "account/order";}
 @PostMapping("/orders/{id}/cancel") public String cancel(Principal p,@PathVariable Long id,org.springframework.web.servlet.mvc.support.RedirectAttributes flash){try{orders.cancel(p.getName(),id);flash.addFlashAttribute("success","Đơn đã hủy. Tồn kho được hoàn; tiền đã trả cần cửa hàng đối soát hoàn riêng.");}catch(BusinessException e){flash.addFlashAttribute("warning",e.getMessage());}return "redirect:/orders/"+id;}
 @PostMapping("/orders/{id}/reorder") public String reorder(Principal p,@PathVariable Long id,HttpSession session,org.springframework.web.servlet.mvc.support.RedirectAttributes flash){
  var order=orders.owned(p.getName(),id);String message=cart.reorder(order,session);java.util.Map<Long,Integer> selected=new java.util.TreeMap<>();
  for(var line:order.getAccessories()){var a=line.getAccessory();if(a.getActive()&&a.getStock()>0)selected.put(a.getId(),Math.min(20,Math.min(line.getQuantity(),a.getStock())));}
  session.setAttribute("reorderAccessories",selected);flash.addFlashAttribute("success",message);return "redirect:/cart";
 }
}
