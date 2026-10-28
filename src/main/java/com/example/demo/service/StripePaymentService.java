package com.example.demo.service;
import com.example.demo.config.StripeSettings;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.stripe.model.checkout.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class StripePaymentService {
 private final StripePaymentRepository attempts;private final CustomerOrderRepository orders;private final CustomCakeRequestRepository custom;private final PaymentRecordRepository records;private final NotificationService notices;private final AccountService accounts;private final StripeGateway gateway;private final StripeSettings settings;private final TransactionTemplate tx;
 private record Parent(User user,PaymentSummary payment,BigDecimal total,boolean eligible,boolean cancelled){}
 public StripePaymentService(StripePaymentRepository attempts,CustomerOrderRepository orders,CustomCakeRequestRepository custom,PaymentRecordRepository records,NotificationService notices,AccountService accounts,StripeGateway gateway,StripeSettings settings,PlatformTransactionManager manager){this.attempts=attempts;this.orders=orders;this.custom=custom;this.records=records;this.notices=notices;this.accounts=accounts;this.gateway=gateway;this.settings=settings;tx=new TransactionTemplate(manager);}
 private Parent parent(Long orderId,Long customId){
  if((orderId==null)==(customId==null))throw new BusinessException("Chọn đúng một đơn hàng cần thanh toán.");
  if(orderId!=null){var o=orders.lockById(orderId).orElseThrow(()->new BusinessException("Không tìm thấy đơn hàng."));return new Parent(o.getUser(),o.getPayment(),o.getTotal(),o.getPaymentMethod()==PaymentMethod.STRIPE&&o.getStatus()!=OrderStatus.CANCELLED&&o.getStatus()!=OrderStatus.COMPLETED,o.getStatus()==OrderStatus.CANCELLED);}
  var r=custom.lockById(customId).orElseThrow(()->new BusinessException("Không tìm thấy bánh riêng."));return new Parent(r.getUser(),r.getPayment(),r.getQuotedTotal(),r.getPaymentMethod()==PaymentMethod.STRIPE&&Set.of(CustomCakeStatus.ACCEPTED,CustomCakeStatus.PREPARING,CustomCakeStatus.READY).contains(r.getStatus()),r.getStatus()==CustomCakeStatus.CANCELLED||r.getStatus()==CustomCakeStatus.REJECTED);
 }
 private Parent parent(StripePayment p){return parent(p.getOrder()==null?null:p.getOrder().getId(),p.getCustomCake()==null?null:p.getCustomCake().getId());}
 private List<StripePayment> history(Long orderId,Long customId){return orderId!=null?attempts.findByOrderIdOrderByCreatedAtDesc(orderId):attempts.findByCustomCakeIdOrderByCreatedAtDesc(customId);}
 private void requireOwner(User u,String email){if(!u.getId().equals(accounts.current(email).getId()))throw new BusinessException("Không tìm thấy giao dịch của bạn.");}
 public String start(String email,Long orderId,Long customId){
  if(!settings.ready())throw new BusinessException("Stripe Sandbox chưa sẵn sàng. Cần cấu hình khóa thử và kết nối webhook trước khi thanh toán.");
  StripePayment attempt=Objects.requireNonNull(tx.execute(status->{var p=parent(orderId,customId);requireOwner(p.user(),email);if(!p.eligible()||p.total()==null||p.payment().getRefunded().signum()>0)throw new BusinessException("Đơn này chưa đủ điều kiện thanh toán Stripe hoặc đã đóng.");
   if(p.payment().remaining(p.total()).signum()<=0)throw new BusinessException("Đơn đã thanh toán đủ.");
   if(history(orderId,customId).stream().anyMatch(a->a.getStatus()==StripePayment.Status.REVIEW))throw new BusinessException("Đơn có khoản Stripe cần cửa hàng đối soát. Không thanh toán lại trước khi được xử lý.");
   for(var a:history(orderId,customId))if(a.getStatus()==StripePayment.Status.CREATING||a.getStatus()==StripePayment.Status.OPEN){return a;}
   BigDecimal remaining=p.payment().remaining(p.total()),amount=p.payment().getUpfrontRemaining().signum()>0?p.payment().getUpfrontRemaining().min(remaining):remaining;
   if(amount.signum()<=0)throw new BusinessException("Đơn đã thanh toán đủ, không tạo thêm phiên thu tiền.");
   var a=new StripePayment();a.setId(UUID.randomUUID().toString());a.setUser(p.user());if(orderId!=null)a.setOrder(orders.getReferenceById(orderId));else a.setCustomCake(custom.getReferenceById(customId));a.setAmount(amount);return attempts.saveAndFlush(a);
  }));
  if(attempt.getStatus()==StripePayment.Status.OPEN&&attempt.getCheckoutUrl()!=null)return attempt.getCheckoutUrl();
  // Mã chống trùng được lưu trước khi gọi Stripe; thử lại dùng cùng mã và cùng tham số.
  Session session;
  try{session=gateway.create(attempt);}catch(StripeGateway.Rejected e){tx.executeWithoutResult(status->{parent(attempt);var locked=attempts.lockById(attempt.getId()).orElseThrow();if(locked.getStatus()==StripePayment.Status.CREATING&&locked.getSessionId()==null){locked.setStatus(StripePayment.Status.EXPIRED);locked.setNote("Stripe từ chối tạo phiên; chưa ghi nhận khoản thu.");}});throw e;}
  return Objects.requireNonNull(tx.execute(status->{var p=parent(attempt);var a=attempts.lockById(attempt.getId()).orElseThrow();if(a.getStatus()==StripePayment.Status.PAID||a.getStatus()==StripePayment.Status.REVIEW||a.getStatus()==StripePayment.Status.RESOLVED)return "/payments/stripe/"+a.getId();
   if(session.getAmountTotal()==null||session.getAmountTotal()!=a.getAmount().longValueExact()||!"vnd".equals(session.getCurrency())||!a.getId().equals(session.getClientReferenceId()))throw new BusinessException("Phiên Stripe trả về không khớp đơn hàng.");
   if(a.getSessionId()!=null&&!a.getSessionId().equals(session.getId()))throw new BusinessException("Phiên Stripe không khớp lần thanh toán.");
   a.setSessionId(session.getId());a.setCheckoutUrl(session.getUrl());a.setExpiresAt(session.getExpiresAt()==null?null:Instant.ofEpochSecond(session.getExpiresAt()));a.setStatus("expired".equals(session.getStatus())?StripePayment.Status.EXPIRED:StripePayment.Status.OPEN);
   return p.eligible()&&a.getStatus()==StripePayment.Status.OPEN&&a.getCheckoutUrl()!=null?a.getCheckoutUrl():"/payments/stripe/"+a.getId();
  }));
 }
 public StripePayment owned(String email,String id){var a=attempts.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy giao dịch."));var u=accounts.current(email);if(u.getRole()!=Role.ADMIN&&!a.getUser().getId().equals(u.getId()))throw new BusinessException("Không tìm thấy giao dịch của bạn.");return a;}
 public void close(String email,String id){var a=owned(email,id);requireOwner(a.getUser(),email);if(a.getStatus()!=StripePayment.Status.OPEN||a.getSessionId()==null)throw new BusinessException("Phiên chưa ở trạng thái có thể đóng. Hãy chờ cập nhật hoặc tiếp tục phiên cũ.");Session current=gateway.retrieve(a.getSessionId());Session expired="expired".equals(current.getStatus())?current:gateway.expire(a.getSessionId());if(!"expired".equals(expired.getStatus()))throw new BusinessException("Chưa xác nhận được phiên đã đóng.");tx.executeWithoutResult(status->{parent(a);var locked=attempts.lockById(id).orElseThrow();if(locked.getStatus()==StripePayment.Status.OPEN){locked.setStatus(StripePayment.Status.EXPIRED);locked.setCheckoutUrl(null);}});}
 public void event(String type,JsonNode data){
  if(!Set.of("checkout.session.completed","checkout.session.async_payment_succeeded","checkout.session.expired","checkout.session.async_payment_failed").contains(type))return;
  String id=data.path("metadata").path("attempt_id").asText("");if(!id.matches("[a-f0-9-]{36}"))return;var found=attempts.findById(id);if(found.isEmpty())return;
  tx.executeWithoutResult(status->{Parent p=parent(found.get());var a=attempts.lockById(id).orElseThrow();if(a.getStatus()==StripePayment.Status.PAID||a.getStatus()==StripePayment.Status.REVIEW||a.getStatus()==StripePayment.Status.RESOLVED)return;
   String sid=data.path("id").asText("");if(!sid.startsWith("cs_test_")||data.path("livemode").asBoolean(true)||!"payment".equals(data.path("mode").asText())||!id.equals(data.path("client_reference_id").asText())||(a.getSessionId()!=null&&!sid.equals(a.getSessionId())))throw new BusinessException("Dữ liệu phiên Stripe không hợp lệ.");
   a.setSessionId(sid);
   if(type.equals("checkout.session.expired")||type.equals("checkout.session.async_payment_failed")){a.setStatus(StripePayment.Status.EXPIRED);a.setCheckoutUrl(null);return;}
   if(!"paid".equals(data.path("payment_status").asText()))return;
   a.setPaidAt(Instant.now());a.setCheckoutUrl(null);a.setPaymentIntent(data.path("payment_intent").isTextual()?data.path("payment_intent").asText():null);
   if(!data.path("amount_total").isIntegralNumber()||!data.path("amount_total").canConvertToLong()||data.path("amount_total").asLong()!=a.getAmount().longValueExact()||!"vnd".equals(data.path("currency").asText())||a.getPaymentIntent()==null||!a.getPaymentIntent().startsWith("pi_")){
    review(a,"Stripe đã báo trả tiền nhưng số tiền/tiền tệ hoặc mã giao dịch không khớp. Chưa cộng vào tiền đã nhận của đơn.");return;
   }
   if(p.total()==null||a.getAmount().compareTo(p.payment().remaining(p.total()))>0){review(a,"Khoản Stripe có thể trùng với khoản đã đối soát hoặc vượt số còn phải trả. Chưa tự cộng vào đơn; cần kiểm tra trên Stripe Sandbox.");return;}
   if(records.findByToken(a.getId()).isEmpty()){
    var record=new PaymentRecord();record.setOrder(a.getOrder());record.setCustomCake(a.getCustomCake());record.setKind(PaymentRecord.Kind.RECEIPT);record.setAmount(a.getAmount());record.setReference("STRIPE SANDBOX · "+a.getSessionId());record.setActor("STRIPE_SANDBOX");record.setToken(a.getId());records.saveAndFlush(record);p.payment().setPaid(p.payment().getPaid().add(a.getAmount()));
   }
   if(p.cancelled()){review(a,"Đã nhận khoản thanh toán thử cho đơn đã hủy. Đã ghi nhận khoản thu để cửa hàng đối soát/hoàn; không mở lại đơn hoặc giữ lại hàng.");return;}
   a.setStatus(StripePayment.Status.PAID);a.setNote("Stripe Sandbox đã xác nhận; không phát sinh tiền thật.");notices.send(a.getUser(),"Đã nhận thanh toán thử Stripe", "Đã ghi nhận "+a.getAmount()+" đ cho đơn của bạn qua Stripe Sandbox. Không phát sinh tiền thật.",a.getOrderUrl());notices.admins("Stripe Sandbox đã xác nhận", "Đã ghi nhận khoản thu thử "+a.getAmount()+" đ. Kiểm tra đơn trước khi xác nhận/làm bánh.",a.getAdminUrl());
  });
 }
 private void review(StripePayment a,String note){a.setStatus(StripePayment.Status.REVIEW);a.setNote(note);notices.admins("Stripe Sandbox cần đối soát",note,a.getAdminUrl());notices.send(a.getUser(),"Thanh toán Stripe cần kiểm tra","Cửa hàng đã nhận thông báo Stripe Sandbox và đang đối soát. Không thanh toán lại cho đến khi được xác nhận.",a.getOrderUrl());}
 public void resolve(String email,String id,String note,boolean confirmed){if(accounts.current(email).getRole()!=Role.ADMIN)throw new BusinessException("Chỉ admin được đối soát.");if(!confirmed||note==null||note.strip().length()<10||note.length()>1000)throw new BusinessException("Xác nhận đã xử lý và nhập kết quả đối soát từ 10 đến 1.000 ký tự.");var a=attempts.findById(id).orElseThrow();tx.executeWithoutResult(status->{parent(a);var locked=attempts.lockById(id).orElseThrow();if(locked.getStatus()!=StripePayment.Status.REVIEW)throw new BusinessException("Chỉ đối soát giao dịch cần kiểm tra.");locked.setStatus(StripePayment.Status.RESOLVED);locked.setResolution(note.strip());locked.setResolvedBy(email);locked.setResolvedAt(Instant.now());notices.send(locked.getUser(),"Đã đối soát Stripe Sandbox",note.strip(),locked.getOrderUrl());});}
}
