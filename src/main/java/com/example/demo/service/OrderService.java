package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.dto.CheckoutForm;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.*;
import java.math.BigDecimal;
@Service
public class OrderService {
 @org.springframework.beans.factory.annotation.Autowired private com.example.demo.config.StripeSettings stripeSettings;
 @org.springframework.beans.factory.annotation.Autowired private jakarta.validation.Validator validator;
 private final AccessoryService accessories;private final NotificationService notices;
 @org.springframework.beans.factory.annotation.Value("${app.shop.address:}") private String shopAddress;
 private final OptionService options;private final StockService stock;private final UserRepository users;private final AddressRepository addresses;private final ProductRepository products;private final CustomerOrderRepository orders;private final VoucherRepository vouchers;private final VoucherUsageRepository usages;private final VoucherService voucherService;private final AccountService accounts;
 public OrderService(UserRepository users,AddressRepository addresses,ProductRepository products,CustomerOrderRepository orders,VoucherRepository vouchers,VoucherUsageRepository usages,VoucherService voucherService,AccountService accounts,OptionService options,StockService stock,AccessoryService accessories,NotificationService notices){this.accessories=accessories;this.notices=notices;this.options=options;this.stock=stock;this.users=users;this.addresses=addresses;this.products=products;this.orders=orders;this.vouchers=vouchers;this.usages=usages;this.voucherService=voucherService;this.accounts=accounts;}
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public CustomerOrder checkout(String email,Map<Long,Integer> cart,CheckoutForm form){
  Map<CartService.Key,Integer> detailed=new TreeMap<>();cart.forEach((id,qty)->detailed.put(new CartService.Key(id,0L),qty));return checkoutDetailed(email,detailed,form);
 }
 @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED) public CustomerOrder checkoutDetailed(String email,Map<CartService.Key,Integer> cart,CheckoutForm form){
  // Serialize orders from the same account, then lock products in stable ID order.
  User user=users.lockById(accounts.current(email).getId()).orElseThrow();
  if(!user.getEnabled())throw new BusinessException("Tài khoản đã bị khóa.");
  var prior=orders.findByCheckoutTokenAndUserId(form.getToken(),user.getId());if(prior.isPresent())return prior.get();
  var violations=validator.validate(form);if(!violations.isEmpty())throw new BusinessException(violations.iterator().next().getMessage());
  if(cart.isEmpty())throw new BusinessException("Giỏ hàng đang trống.");
  if(cart.size()>50)throw new BusinessException("Giỏ hàng tối đa 50 sản phẩm.");
  validateDelivery(form);if(form.getPaymentMethod()==PaymentMethod.STRIPE&&!stripeSettings.ready())throw new BusinessException("Stripe Sandbox chưa sẵn sàng. Vui lòng chọn phương thức khác hoặc cấu hình khóa/webhook.");
  Address address=null;
  if(form.getFulfillmentMethod()==null)throw new BusinessException("Chọn hình thức nhận bánh.");
  if(form.getFulfillmentMethod()==FulfillmentMethod.DELIVERY){if(form.getUseNewAddress()){
   var f=form.getNewAddress();if(f==null||!validator.validate(f).isEmpty())throw new BusinessException("Kiểm tra người nhận, số điện thoại và địa chỉ mới (tối đa 500 ký tự).");GeoService.validate(f.getLatitude(),f.getLongitude());address=new Address();address.setRecipient(f.getRecipient().strip());address.setPhone(f.getPhone().strip());address.setDetail(f.getDetail().strip());address.setLatitude(f.getLatitude());address.setLongitude(f.getLongitude());
  }else{if(form.getAddressId()==null)throw new BusinessException("Chọn địa chỉ giao hàng hoặc nhập địa chỉ mới.");address=addresses.findByIdAndUserId(form.getAddressId(),user.getId()).orElseThrow(()->new BusinessException("Địa chỉ giao hàng không hợp lệ."));}}
  else if(form.getPickupRecipient()==null||form.getPickupRecipient().isBlank()||form.getPickupRecipient().length()>100||form.getPickupPhone()==null||!form.getPickupPhone().matches("0[35789][0-9]{8}"))throw new BusinessException("Nhập họ tên và số di động Việt Nam 10 số của người đến nhận bánh.");
  if(address!=null){String phone=VietnamPhone.normalize(address.getPhone());if(phone==null||!phone.matches("0[35789][0-9]{8}"))throw new BusinessException("SĐT của địa chỉ đã lưu chưa hợp lệ. Hãy sửa trong Trang cá nhân → Địa chỉ giao hàng hoặc nhập địa chỉ mới (số di động Việt Nam 10 số).");address.setPhone(phone);GeoService.validate(address.getLatitude(),address.getLongitude());}
  CustomerOrder order=new CustomerOrder();order.setUser(user);order.setCheckoutToken(form.getToken());order.setFulfillmentMethod(form.getFulfillmentMethod());order.setPreorder(form.getPreorder());
  if(address!=null){order.setRecipient(address.getRecipient());order.setPhone(address.getPhone());order.setDeliveryAddress(address.getDetail());order.setDeliveryLatitude(address.getLatitude());order.setDeliveryLongitude(address.getLongitude());}
  else{order.setRecipient(form.getPickupRecipient().strip());order.setPhone(form.getPickupPhone().strip());order.setDeliveryAddress(shopAddress==null||shopAddress.isBlank()?"Bóng Bakery — cửa hàng sẽ xác nhận địa chỉ nhận bánh":shopAddress);}order.setPaymentMethod(form.getFulfillmentMethod()==FulfillmentMethod.PICKUP&&form.getPaymentMethod()!=PaymentMethod.STRIPE?PaymentMethod.BANK_TRANSFER:form.getPaymentMethod());order.setNote(form.getNote());order.setDeliveryDate(form.getDeliveryDate());order.setDeliverySlot(form.getDeliverySlot());order.setCakeMessage(form.getCakeMessage());
  BigDecimal subtotal=BigDecimal.ZERO;
  for(var entry:new TreeMap<>(cart).entrySet()){
   Product p=products.lockById(entry.getKey().productId()).orElseThrow(()->new BusinessException("Sản phẩm không còn tồn tại."));int qty=entry.getValue();
   if(!p.getActive()||qty<1||qty>99||p.getStock()<qty)throw new BusinessException("Không đủ hàng cho "+p.getName()+". Vui lòng cập nhật giỏ hàng.");
   var choice=options.select(p,entry.getKey().optionId());
   OrderItem item=new OrderItem();item.setOrder(order);item.setProduct(p);item.setProductName(p.getName());item.setUnitPrice(choice.price());item.setOptionLabel(choice.label());item.setOptionId(entry.getKey().optionId());item.setQuantity(qty);order.getItems().add(item);subtotal=subtotal.add(item.getLineTotal());p.setStock(p.getStock()-qty);stock.record(p,-qty,"Đặt hàng",email);
  }
  BigDecimal cakeSubtotal=subtotal;subtotal=subtotal.add(accessories.attach(order,form.getAccessories()));
  Voucher voucher=null;BigDecimal discount=BigDecimal.ZERO;String code=voucherService.normalize(form.getVoucherCode());
  if(!code.isEmpty()){
   voucher=vouchers.lockByCode(code).orElseThrow(()->new BusinessException("Mã giảm giá không tồn tại."));discount=voucherService.discount(voucher,user.getId(),cakeSubtotal);voucher.setUsedCount(voucher.getUsedCount()+1);order.setVoucherCode(code);
  }
  order.setSubtotal(subtotal);order.setDiscount(discount);order.setShippingFee(form.getFulfillmentMethod()==FulfillmentMethod.PICKUP?BigDecimal.ZERO:shipping(cakeSubtotal));order.setTotal(subtotal.subtract(discount).add(order.getShippingFee()));PaymentPolicy.configure(order.getPayment(),order.getTotal(),order.getFulfillmentMethod(),order.getPaymentMethod(),form.getPaymentPlan());orders.saveAndFlush(order);user.setLastFulfillmentMethod(form.getFulfillmentMethod());user.setLastDeliverySlot(form.getDeliverySlot());
  if(voucher!=null){VoucherUsage usage=new VoucherUsage();usage.setUser(user);usage.setVoucher(voucher);usage.setOrder(order);usages.save(usage);}
  String summary=order.getItems().stream().map(i->i.getProductName()+" ("+i.getOptionLabel()+") × "+i.getQuantity()).collect(java.util.stream.Collectors.joining("; "));
  if(!order.getAccessories().isEmpty())summary+="; "+order.getAccessories().stream().map(i->i.getName()+" × "+i.getQuantity()).collect(java.util.stream.Collectors.joining("; "));
  notices.admins("Đơn bánh mới #"+order.getId(),user.getName()+" đã đặt: "+summary+". "+order.getFulfillmentMethod().getLabel()+", "+order.getDeliveryDate()+" "+order.getDeliverySlot()+". Tổng "+order.getTotal()+" đ.","/admin/orders/"+order.getId());
  notices.send(user,"Đã tiếp nhận đơn #"+order.getId(),"Đơn của bạn đang chờ xác nhận. "+order.getFulfillmentMethod().getLabel()+" ngày "+order.getDeliveryDate()+" "+order.getDeliverySlot()+".","/orders/"+order.getId());
  return order;
 }
 public static void validateDelivery(CheckoutForm f){
  java.time.LocalDate today=BakeryClock.today();
  if(f.getDeliveryDate()==null||!f.getDeliveryDate().isAfter(today)||f.getDeliveryDate().isAfter(today.plusDays(f.getPreorder()?90:30)))throw new BusinessException("Chọn ngày nhận từ ngày mai đến 30 ngày tới; bật đặt trước để chọn đến 90 ngày.");
  if(f.getDeliverySlot()==null||!java.util.Set.of("09:00-12:00","13:00-17:00","18:00-20:00").contains(f.getDeliverySlot()))throw new BusinessException("Vui lòng chọn khung giờ nhận bánh.");
 }
 public static BigDecimal shipping(BigDecimal subtotal){return subtotal.compareTo(new BigDecimal("500000"))>=0?BigDecimal.ZERO:new BigDecimal("30000");}
 public Page<CustomerOrder> history(String email,int page){return orders.findByUserIdOrderByCreatedAtDesc(accounts.current(email).getId(),PageRequest.of(Math.max(0,page),10));}
 public CustomerOrder owned(String email,Long id){return orders.findByIdAndUserId(id,accounts.current(email).getId()).orElseThrow(()->new BusinessException("Không tìm thấy đơn hàng của bạn."));}
 @Transactional public void cancel(String email,Long id){
  User user=accounts.current(email);CustomerOrder o=orders.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn hàng."));
  if(!o.getUser().getId().equals(user.getId()))throw new BusinessException("Bạn không có quyền hủy đơn hàng này.");
  move(o,OrderStatus.CANCELLED,email);
 }
 @Transactional public void transition(Long id,OrderStatus next){transition(id,next,"admin");}
 @Transactional public void transition(Long id,OrderStatus next,String actor){move(orders.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn hàng.")),next,actor);}
 private void move(CustomerOrder o,OrderStatus next,String actor){
  if(next==OrderStatus.CANCELLED&&o.getStatus()==OrderStatus.CANCELLED)return;
  if(!o.canMoveTo(next))throw new BusinessException("Không thể chuyển trạng thái đơn hàng theo bước này.");
  if(next==OrderStatus.CONFIRMED||next==OrderStatus.PREPARING)PaymentPolicy.requireUpfront(o.getPayment());
  if(next==OrderStatus.COMPLETED){
   PaymentPolicy.requireSettled(o.getPayment(),o.getTotal());
   if(o.getFulfillmentMethod()==FulfillmentMethod.DELIVERY&&o.getHandedOverAt()==null)throw new BusinessException("Shipper chưa xác nhận bàn giao bánh. Chỉ hoàn tất sau khi đã giao và đối soát đủ tiền.");
  }
  if(next==OrderStatus.CANCELLED){
   o.getItems().stream().sorted(Comparator.comparing(i->i.getProduct().getId())).forEach(i->{Product p=products.lockById(i.getProduct().getId()).orElseThrow();p.setStock(Math.addExact(p.getStock(),i.getQuantity()));stock.record(p,i.getQuantity(),"Hoàn kho do hủy đơn #"+o.getId(),actor);});
   accessories.restore(o);
   // Voucher uses intentionally remain consumed after cancellation; see design specification.
  }
  if(next==OrderStatus.SHIPPING&&(o.getShipper()==null||!o.getShipper().getEnabled()||o.getShipper().getRole()!=Role.SHIPPER))throw new BusinessException("Hãy phân công shipper đang hoạt động trước khi chuyển sang đang giao.");
  if(next==OrderStatus.CANCELLED||next==OrderStatus.COMPLETED)DeliveryService.clearLocation(o);
  o.setStatus(next);
  if(next==OrderStatus.COMPLETED)o.setCompletedAt(BakeryClock.now());
  String message=switch(next){case CONFIRMED->"Cửa hàng đã xác nhận đơn và lịch nhận bánh.";case PREPARING->"Bóng Bakery đang chuẩn bị bánh cho bạn.";case SHIPPING->"Bánh đang được giao đến địa chỉ của bạn. Vui lòng chú ý điện thoại.";case READY_FOR_PICKUP->"Bánh đã sẵn sàng. Bạn có thể đến nhận theo lịch tại: "+o.getDeliveryAddress();case COMPLETED->"Đơn bánh đã hoàn thành. Bạn có thể vào chi tiết bánh để gửi đánh giá.";case CANCELLED->"Đơn đã hủy. Tồn kho đã hoàn lại; lượt mã giảm giá không được hoàn. Nếu đã chuyển tiền, khoản hoàn cần cửa hàng đối soát và xử lý riêng.";default->"Trạng thái đơn: "+next.getLabel();};
  if(next==OrderStatus.CANCELLED&&o.getPayment().getPaid().signum()>0)message+=" Cần hoàn "+o.getPayment().getPaid()+" đ; cửa hàng phải hoàn thực tế và lưu mã giao dịch. Theo dõi tại chi tiết đơn.";
  notices.send(o.getUser(),"Đơn #"+o.getId()+": "+next.getLabel(),message,"/orders/"+o.getId());
  if(next==OrderStatus.CANCELLED)notices.admins("Đơn #"+o.getId()+" đã hủy","Đơn của "+o.getUser().getName()+" đã hủy bởi "+actor+"."+(o.getPayment().getPaid().signum()>0?" CẦN HOÀN "+o.getPayment().getPaid()+" đ. Xử lý trong mục Hoàn tiền sau hủy.":""),"/admin/orders/"+o.getId());
 }
}
