package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.dto.CustomCakeForm;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.time.*;
import java.math.BigDecimal;

@Service
public class CustomCakeService {
 @org.springframework.beans.factory.annotation.Autowired private jakarta.validation.Validator validator;
 @org.springframework.beans.factory.annotation.Autowired private com.example.demo.config.StripeSettings stripeSettings;
 private final CakeDesignService designs;private final CustomCakeRequestRepository requests;private final AccountService accounts;private final UserRepository users;private final NotificationService notices;private final MediaService media;
 @Value("${app.shop.address:}") private String shopAddress;
 public CustomCakeService(CustomCakeRequestRepository requests,AccountService accounts,UserRepository users,NotificationService notices,MediaService media,CakeDesignService designs){this.designs=designs;this.requests=requests;this.accounts=accounts;this.users=users;this.notices=notices;this.media=media;}
 public CustomCakeRequest owned(String email,Long id){return requests.findByIdAndUserId(id,accounts.current(email).getId()).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu bánh của bạn."));}
 @Transactional public CustomCakeRequest submit(String email,CustomCakeForm f,MultipartFile image){
  var violations=validator.validate(f);if(!violations.isEmpty())throw new BusinessException(violations.iterator().next().getMessage());
  if(f.getFulfillmentMethod()==FulfillmentMethod.DELIVERY)GeoService.validate(f.getLatitude(),f.getLongitude());
  if(f.getPaymentMethod()==PaymentMethod.STRIPE&&!stripeSettings.ready())throw new BusinessException("Stripe Sandbox chưa sẵn sàng. Cần cấu hình khóa thử và webhook.");
  User u=users.lockById(accounts.current(email).getId()).orElseThrow();var prior=requests.findByTokenAndUserId(f.getToken(),u.getId());if(prior.isPresent())return prior.get();
  if(f.getDescription().strip().length()<20)throw new BusinessException("Vui lòng mô tả thiết kế từ 20 ký tự có nội dung.");
  LocalDate today=LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));if(f.getReceiveDate().isBefore(today.plusDays(3))||f.getReceiveDate().isAfter(today.plusDays(90)))throw new BusinessException("Bánh thiết kế riêng cần đặt trước từ 3 đến 90 ngày.");
  if(f.getFulfillmentMethod()==FulfillmentMethod.DELIVERY&&(f.getAddress()==null||f.getAddress().isBlank()))throw new BusinessException("Nhập địa chỉ giao bánh.");
  CustomCakeRequest r=new CustomCakeRequest();r.setUser(u);if(f.getFulfillmentMethod()==FulfillmentMethod.DELIVERY){r.setLatitude(f.getLatitude());r.setLongitude(f.getLongitude());}r.setRecipient(f.getRecipient().strip());r.setPhone(f.getPhone().strip());r.setDescription(f.getDescription().strip());r.setFlavor(f.getFlavor().strip());r.setServings(f.getServings());r.setBudget(f.getBudget());r.setReceiveDate(f.getReceiveDate());r.setReceiveSlot(f.getReceiveSlot());r.setFulfillmentMethod(f.getFulfillmentMethod());r.setAddress(f.getFulfillmentMethod()==FulfillmentMethod.PICKUP?pickupAddress():f.getAddress().strip());r.setPaymentMethod(f.getFulfillmentMethod()==FulfillmentMethod.PICKUP&&f.getPaymentMethod()!=PaymentMethod.STRIPE?PaymentMethod.BANK_TRANSFER:f.getPaymentMethod());r.getPayment().setPlan(f.getFulfillmentMethod()==FulfillmentMethod.PICKUP?f.getPaymentPlan():((f.getPaymentMethod()==PaymentMethod.BANK_TRANSFER||f.getPaymentMethod()==PaymentMethod.STRIPE)?PaymentPlan.FULL:PaymentPlan.ON_RECEIPT));if(f.getFulfillmentMethod()==FulfillmentMethod.PICKUP&&f.getPaymentPlan()!=PaymentPlan.DEPOSIT&&f.getPaymentPlan()!=PaymentPlan.FULL)throw new BusinessException("Chọn cọc 50% hoặc trả đủ cho bánh nhận tại cửa hàng.");r.setCakeMessage(f.getCakeMessage());r.setToken(f.getToken());if(image!=null&&!image.isEmpty())r.setReferenceImage(media.saveReviewPhoto(image));requests.saveAndFlush(r);
  notices.send(u,"Đã nhận yêu cầu bánh riêng #"+r.getId(),"Cửa hàng sẽ xem thiết kế và báo tổng giá. Chỉ bắt đầu làm sau khi bạn đồng ý báo giá.","/custom-cakes/"+r.getId());
  notices.admins("Yêu cầu bánh thiết kế #"+r.getId(),u.getName()+" đặt bánh cho "+r.getServings()+" người, vị "+r.getFlavor()+", nhận "+r.getReceiveDate()+". "+r.getDescription(),"/admin/custom-cakes/"+r.getId());return r;
 }
 private String pickupAddress(){return shopAddress==null||shopAddress.isBlank()?"Bóng Bakery — cửa hàng sẽ xác nhận địa chỉ nhận bánh":shopAddress;}
 @Transactional public void customerAction(String email,Long id,boolean accept){
  User u=accounts.current(email);CustomCakeRequest r=requests.lockById(id).orElseThrow();if(!r.getUser().getId().equals(u.getId()))throw new BusinessException("Bạn không có quyền thao tác yêu cầu này.");
  if(accept){if(r.getStatus()!=CustomCakeStatus.QUOTED||!r.getReceiveDate().isAfter(LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))))throw new BusinessException("Báo giá không còn ở trạng thái chờ xác nhận hoặc đã quá ngày nhận.");r.setStatus(CustomCakeStatus.ACCEPTED);}
  else{if(r.getStatus()!=CustomCakeStatus.SENT&&r.getStatus()!=CustomCakeStatus.QUOTED)throw new BusinessException("Yêu cầu đã được xác nhận, vui lòng liên hệ cửa hàng.");r.setStatus(CustomCakeStatus.CANCELLED);}
  r.setUpdatedAt(BakeryClock.now());notices.admins("Bánh riêng #"+id+": "+r.getStatus().getLabel(),u.getName()+(accept?" đã đồng ý báo giá "+r.getQuotedTotal()+" đ.":" đã hủy yêu cầu."),"/admin/custom-cakes/"+id);notices.send(u,"Bánh riêng #"+id+": "+r.getStatus().getLabel(),accept?"Cửa hàng đã nhận xác nhận báo giá. Bánh chỉ bắt đầu làm sau khi bạn duyệt mẫu mới nhất và đã đối soát đủ khoản trả trước.":"Yêu cầu bánh riêng đã hủy.","/custom-cakes/"+id);
 }
 @Transactional public void update(Long id,CustomCakeStatus next,BigDecimal total,String reply){
  CustomCakeRequest r=requests.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu."));CustomCakeStatus current=r.getStatus();
  boolean allowed=(current==CustomCakeStatus.SENT&&(next==CustomCakeStatus.QUOTED||next==CustomCakeStatus.REJECTED))||(current==CustomCakeStatus.ACCEPTED&&next==CustomCakeStatus.PREPARING)||(current==CustomCakeStatus.PREPARING&&next==CustomCakeStatus.READY)||(current==CustomCakeStatus.READY&&next==CustomCakeStatus.COMPLETED);
  if(next==CustomCakeStatus.PREPARING){designs.requireApproved(id);PaymentPolicy.requireUpfront(r.getPayment());}
  if(next==CustomCakeStatus.COMPLETED)PaymentPolicy.requireSettled(r.getPayment(),r.getQuotedTotal());
  if(!allowed)throw new BusinessException("Không thể chuyển trạng thái yêu cầu theo bước này.");
  if(reply!=null&&reply.length()>1500)throw new BusinessException("Phản hồi tối đa 1.500 ký tự.");
  if(next==CustomCakeStatus.QUOTED){if(total==null||total.compareTo(new BigDecimal("10000"))<0||total.compareTo(new BigDecimal("999999999"))>0||total.stripTrailingZeros().scale()>0||reply==null||reply.isBlank())throw new BusinessException("Nhập tổng báo giá nguyên đồng và nội dung thiết kế sẽ thực hiện.");if(!r.getReceiveDate().isAfter(LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))))throw new BusinessException("Ngày nhận đã qua. Hãy trao đổi để khách gửi yêu cầu mới.");r.setQuotedTotal(total);PaymentPolicy.configure(r.getPayment(),total,r.getFulfillmentMethod(),r.getPaymentMethod(),r.getPayment().getPlan());r.setAdminReply(reply.strip());}
  if(next==CustomCakeStatus.REJECTED){if(reply==null||reply.isBlank())throw new BusinessException("Nhập lý do không thể đáp ứng.");r.setAdminReply(reply.strip());}
  r.setStatus(next);if(next==CustomCakeStatus.COMPLETED)r.setCompletedAt(BakeryClock.now());r.setUpdatedAt(BakeryClock.now());String message=next==CustomCakeStatus.QUOTED?"Tổng báo giá: "+total+" đ (đã gồm phụ phí/giao hàng nếu có). "+r.getAdminReply():"Yêu cầu bánh thiết kế của bạn: "+next.getLabel()+(next==CustomCakeStatus.REJECTED?". "+r.getAdminReply():".");
  notices.send(r.getUser(),"Bánh riêng #"+id+": "+next.getLabel(),message,"/custom-cakes/"+id);
 }
}
