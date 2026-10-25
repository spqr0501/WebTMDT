package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AfterSalesService {
 @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
 private final AfterSalesRepository cases;private final CustomerOrderRepository orders;private final CustomCakeRequestRepository custom;
 private final AccountService accounts;private final NotificationService notices;private final MediaService media;
 public AfterSalesService(AfterSalesRepository cases,CustomerOrderRepository orders,CustomCakeRequestRepository custom,AccountService accounts,NotificationService notices,MediaService media){this.cases=cases;this.orders=orders;this.custom=custom;this.accounts=accounts;this.notices=notices;this.media=media;}
 public AfterSalesRequest owned(Long id,String email){return cases.findByIdAndUserId(id,accounts.current(email).getId()).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu đổi trả của bạn."));}
 @Transactional public AfterSalesRequest submit(Long parentId,boolean isCustom,String email,String reason,AfterSalesRequest.Resolution resolution,List<MultipartFile> photos){
  User user=accounts.current(email);CustomerOrder o=null;CustomCakeRequest r=null;LocalDateTime completed;boolean finished;
  if(isCustom){r=custom.lockById(parentId).orElseThrow(()->new BusinessException("Không tìm thấy đơn bánh."));if(!r.getUser().getId().equals(user.getId()))throw new BusinessException("Bạn không có quyền gửi yêu cầu cho đơn này.");var prior=cases.findByCustomCakeId(parentId);if(prior.isPresent())return prior.get();completed=r.getCompletedAt();finished=r.getStatus()==CustomCakeStatus.COMPLETED;}
  else{o=orders.lockById(parentId).orElseThrow(()->new BusinessException("Không tìm thấy đơn bánh."));if(!o.getUser().getId().equals(user.getId()))throw new BusinessException("Bạn không có quyền gửi yêu cầu cho đơn này.");var prior=cases.findByOrderId(parentId);if(prior.isPresent())return prior.get();completed=o.getCompletedAt();finished=o.getStatus()==OrderStatus.COMPLETED;}
  if(!finished)throw new BusinessException("Chỉ yêu cầu đổi trả sau khi đơn đã hoàn thành. Nếu chưa nhận bánh, hãy liên hệ hỗ trợ.");
  if(completed==null)throw new BusinessException("Đơn cũ chưa có thời điểm bàn giao. Vui lòng gửi phiếu hỗ trợ để cửa hàng xác minh.");
  if(LocalDateTime.now().isAfter(completed.plusHours(24)))throw new BusinessException("Đã quá 24 giờ từ khi bàn giao. Hãy gửi phiếu hỗ trợ để cửa hàng xem xét riêng.");
  if(reason==null||reason.strip().length()<20||reason.length()>2000||resolution==null)throw new BusinessException("Chọn phương án và mô tả vấn đề từ 20 đến 2.000 ký tự.");
  var files=photos==null?List.<MultipartFile>of():photos.stream().filter(f->!f.isEmpty()).toList();
  if(files.isEmpty()||files.size()>3)throw new BusinessException("Gửi từ 1 đến 3 ảnh minh chứng JPG/PNG, mỗi ảnh tối đa 5 MB.");
  var c=new AfterSalesRequest();c.setUser(user);c.setOrder(o);c.setCustomCake(r);c.setReason(reason.strip());c.setResolution(resolution);for(var f:files)c.getPhotos().add(media.saveEvidencePhoto(f));cases.saveAndFlush(c);
  notices.admins("Yêu cầu đổi trả #"+c.getId(),user.getName()+" gửi yêu cầu cho đơn "+c.getOrderCode()+". "+c.getReason(),"/admin/after-sales/"+c.getId());
  notices.send(user,"Đã nhận yêu cầu đổi trả #"+c.getId(),"Cửa hàng sẽ kiểm tra ảnh và thông tin. Gửi yêu cầu chưa đồng nghĩa đã được duyệt hoặc hoàn tiền.","/after-sales/"+c.getId());return c;
 }
 private AfterSalesRequest locked(Long id){var c=cases.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu đổi trả."));Object parent=c.getOrder()!=null?orders.lockById(c.getOrder().getId()).orElseThrow():custom.lockById(c.getCustomCake().getId()).orElseThrow();entityManager.refresh(parent,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);entityManager.refresh(c,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);return c;}
 private void admin(String actor){if(accounts.current(actor).getRole()!=Role.ADMIN)throw new BusinessException("Chỉ quản trị viên được xử lý đổi trả.");}
 @Transactional public void decide(Long id,boolean approve,AfterSalesRequest.Resolution resolution,BigDecimal amount,String reply,String actor){
  admin(actor);var c=locked(id);if(c.getStatus()!=AfterSalesRequest.Status.REQUESTED)throw new BusinessException("Yêu cầu đã được xử lý. Vui lòng tải lại trang.");
  text(reply);if(approve){if(resolution==null)throw new BusinessException("Chọn phương án được duyệt.");c.setResolution(resolution);
   if(resolution==AfterSalesRequest.Resolution.REFUND){BigDecimal paid=c.getOrder()!=null?c.getOrder().getPayment().getPaid():c.getCustomCake().getPayment().getPaid();if(amount==null||amount.signum()<=0||amount.stripTrailingZeros().scale()>0||amount.compareTo(paid)>0)throw new BusinessException("Số tiền duyệt hoàn phải là số nguyên dương, không vượt khoản đã nhận chưa hoàn. Hãy đối soát tiền trước.");c.setApprovedAmount(amount);}else c.setApprovedAmount(BigDecimal.ZERO);
  }
  c.setStatus(approve?AfterSalesRequest.Status.APPROVED:AfterSalesRequest.Status.REJECTED);c.setReply(reply.strip());c.setActor(actor);c.setHandledAt(LocalDateTime.now());
  notices.send(c.getUser(),"Đổi trả #"+id+": "+c.getStatusLabel(),c.getReply()+(approve?" Phương án: "+c.getResolutionLabel()+". Duyệt hoàn tiền chưa có nghĩa tiền đã được chuyển.":""),"/after-sales/"+id);
 }
 @Transactional public void completeReplacement(Long id,String reply,String actor){
  admin(actor);var c=locked(id);if(c.getStatus()!=AfterSalesRequest.Status.APPROVED||c.getResolution()!=AfterSalesRequest.Resolution.REPLACEMENT)throw new BusinessException("Chỉ hoàn tất yêu cầu đổi bánh đã được duyệt.");text(reply);c.setReply(c.getReply()+"\nBàn giao: "+reply.strip());if(c.getReply().length()>2000)throw new BusinessException("Tổng nội dung phản hồi và bàn giao tối đa 2.000 ký tự.");c.setActor(actor);c.setCompletedAt(LocalDateTime.now());c.setStatus(AfterSalesRequest.Status.COMPLETED);
  notices.send(c.getUser(),"Đổi trả #"+id+" đã hoàn tất",reply.strip(),"/after-sales/"+id);
 }
 private void text(String s){if(s==null||s.isBlank()||s.length()>1500)throw new BusinessException("Nhập phản hồi / lý do xử lý, tối đa 1.500 ký tự.");}
}
