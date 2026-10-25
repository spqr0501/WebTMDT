package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.dto.PaymentRecordForm;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
@Service
public class PaymentService {
 @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
 private final AfterSalesRepository cases;private final CustomerOrderRepository orders;private final CustomCakeRequestRepository custom;private final PaymentRecordRepository records;private final AccountService accounts;private final NotificationService notices;
 public PaymentService(CustomerOrderRepository orders,CustomCakeRequestRepository custom,PaymentRecordRepository records,AccountService accounts,NotificationService notices,AfterSalesRepository cases){this.cases=cases;this.orders=orders;this.custom=custom;this.records=records;this.accounts=accounts;this.notices=notices;}
 public List<PaymentRecord> orderHistory(Long id){return records.findByOrderIdOrderByCreatedAtAscIdAsc(id);}
 public List<PaymentRecord> customHistory(Long id){return records.findByCustomCakeIdOrderByCreatedAtAscIdAsc(id);}
 @Transactional public void recordOrder(Long id,PaymentRecordForm form,String actor){
  requireAdmin(actor);CustomerOrder o=orders.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn hàng."));
  if(duplicate(form,id,false))return;
  boolean cancelled=o.getStatus()==OrderStatus.CANCELLED;
  apply(o.getPayment(),o.getTotal(),cancelled,form);
  PaymentRecord r=create(form,actor);r.setOrder(o);records.saveAndFlush(r);
  notices.send(o.getUser(),"Thanh toán đơn #"+id,description(form,o.getPayment(),o.getTotal()),"/orders/"+id);
 }
 @Transactional public void recordCustom(Long id,PaymentRecordForm form,String actor){
  requireAdmin(actor);CustomCakeRequest r=custom.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu bánh."));
  if(duplicate(form,id,true))return;
  if(r.getQuotedTotal()==null)throw new BusinessException("Yêu cầu chưa có báo giá.");
  boolean cancelled=r.getStatus()==CustomCakeStatus.CANCELLED||r.getStatus()==CustomCakeStatus.REJECTED;
  if(!cancelled&&(r.getStatus()==CustomCakeStatus.SENT||r.getStatus()==CustomCakeStatus.QUOTED))throw new BusinessException("Chỉ ghi nhận tiền sau khi khách đã đồng ý báo giá.");
  apply(r.getPayment(),r.getQuotedTotal(),cancelled,form);PaymentRecord payment=create(form,actor);payment.setCustomCake(r);records.saveAndFlush(payment);
  notices.send(r.getUser(),"Thanh toán bánh riêng #TK"+id,description(form,r.getPayment(),r.getQuotedTotal()),"/custom-cakes/"+id);
 }
 private void requireAdmin(String actor){if(accounts.current(actor).getRole()!=Role.ADMIN)throw new BusinessException("Chỉ quản trị viên được xác nhận thanh toán.");}
 @Transactional public void refundCase(Long id,PaymentRecordForm f,String actor){
  requireAdmin(actor);var found=cases.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu đổi trả."));
  CustomerOrder o=null;CustomCakeRequest r=null;
  if(found.getOrder()!=null)o=orders.lockById(found.getOrder().getId()).orElseThrow();else r=custom.lockById(found.getCustomCake().getId()).orElseThrow();
  entityManager.refresh(o!=null?o:r,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);entityManager.refresh(found,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);var c=found;Long parent=o!=null?o.getId():r.getId();
  validate(f);if(f.getKind()!=PaymentRecord.Kind.REFUND)throw new BusinessException("Chỉ ghi nhận hoàn tiền ở yêu cầu đổi trả.");
  if(duplicate(f,parent,o==null)){var prior=records.findByToken(f.getToken()).orElseThrow();if(prior.getAfterSales()==null||!prior.getAfterSales().getId().equals(id))throw new BusinessException("Mã giao dịch đã dùng cho yêu cầu khác.");return;}
  if(c.getStatus()!=AfterSalesRequest.Status.APPROVED||c.getResolution()!=AfterSalesRequest.Resolution.REFUND)throw new BusinessException("Chỉ hoàn cho yêu cầu đã duyệt phương án hoàn tiền.");
  if(f.getAmount().compareTo(c.getRefundRemaining())>0)throw new BusinessException("Khoản hoàn vượt số tiền còn được duyệt.");
  PaymentSummary p=o!=null?o.getPayment():r.getPayment();BigDecimal total=o!=null?o.getTotal():r.getQuotedTotal();apply(p,total,true,f);
  PaymentRecord record=create(f,actor);record.setOrder(o);record.setCustomCake(r);record.setAfterSales(c);records.saveAndFlush(record);
  c.setRefunded(c.getRefunded().add(f.getAmount()));c.setActor(actor);
  if(c.getRefundRemaining().signum()==0){c.setStatus(AfterSalesRequest.Status.COMPLETED);c.setCompletedAt(java.time.LocalDateTime.now());}
  notices.send(c.getUser(),"Hoàn tiền đổi trả #"+id,"Cửa hàng đã xác nhận hoàn "+f.getAmount()+" đ. Còn chờ hoàn theo duyệt: "+c.getRefundRemaining()+" đ. Tham chiếu: "+f.getReference(),"/after-sales/"+id);
 }
 private boolean duplicate(PaymentRecordForm f,Long id,boolean isCustom){
  validate(f);
  if(f.getToken()==null)throw new BusinessException("Thiếu mã xác nhận giao dịch.");
  var prior=records.findByToken(f.getToken());if(prior.isEmpty())return false;PaymentRecord p=prior.get();
  boolean same=isCustom?p.getCustomCake()!=null&&p.getCustomCake().getId().equals(id):p.getOrder()!=null&&p.getOrder().getId().equals(id);
  if(!same||p.getKind()!=f.getKind()||p.getAmount().compareTo(f.getAmount())!=0||!p.getReference().equals(f.getReference().strip()))throw new BusinessException("Mã giao dịch đã dùng cho nội dung khác. Tải lại trang trước khi nhập giao dịch mới.");return true;
 }
 private void validate(PaymentRecordForm f){
  if(f.getToken()==null||!f.getToken().matches("[a-fA-F0-9-]{36}"))throw new BusinessException("Thiếu hoặc sai mã giao dịch. Tải lại trang.");
  if(f.getAmount()==null||f.getAmount().signum()<=0||f.getAmount().stripTrailingZeros().scale()>0||f.getReference()==null||f.getReference().isBlank()||f.getReference().length()>200||f.getKind()==null)throw new BusinessException("Nhập số tiền nguyên đồng và mã tham chiếu / ghi chú đối soát.");
 }
 private void apply(PaymentSummary summary,BigDecimal total,boolean cancelled,PaymentRecordForm f){
  validate(f);
  if(f.getKind()==PaymentRecord.Kind.RECEIPT){if(cancelled)throw new BusinessException("Không nhận thêm tiền cho đơn đã hủy.");if(f.getAmount().compareTo(summary.remaining(total))>0)throw new BusinessException("Số tiền nhận vượt số còn phải trả.");summary.setPaid(summary.getPaid().add(f.getAmount()));}
  else{if(!cancelled)throw new BusinessException("Đơn đã hoàn thành cần được duyệt yêu cầu đổi trả trước khi hoàn tiền.");if(f.getAmount().compareTo(summary.getPaid())>0)throw new BusinessException("Số tiền hoàn vượt số đã nhận chưa hoàn.");summary.setPaid(summary.getPaid().subtract(f.getAmount()));summary.setRefunded(summary.getRefunded().add(f.getAmount()));}
 }
 private PaymentRecord create(PaymentRecordForm f,String actor){PaymentRecord r=new PaymentRecord();r.setKind(f.getKind());r.setAmount(f.getAmount());r.setReference(f.getReference().strip());r.setToken(f.getToken());r.setActor(actor);return r;}
 private String description(PaymentRecordForm f,PaymentSummary p,BigDecimal total){return (f.getKind()==PaymentRecord.Kind.RECEIPT?"Cửa hàng đã xác nhận nhận ":"Cửa hàng đã ghi nhận hoàn ")+f.getAmount()+" đ. "+(f.getKind()==PaymentRecord.Kind.RECEIPT?"Còn phải trả: "+p.remaining(total)+" đ.":"Số tiền cửa hàng còn giữ: "+p.getPaid()+" đ.");}
}
