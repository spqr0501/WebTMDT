package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CakeDesignService {
 private final CakeDesignRepository designs; private final CustomCakeRequestRepository requests;
 private final AccountService accounts; private final NotificationService notices; private final MediaService media;
 public CakeDesignService(CakeDesignRepository designs,CustomCakeRequestRepository requests,AccountService accounts,NotificationService notices,MediaService media){this.designs=designs;this.requests=requests;this.accounts=accounts;this.notices=notices;this.media=media;}
 public List<CakeDesign> history(Long id){return designs.findByRequestIdOrderByIdDesc(id);}
 @Transactional public void propose(Long id,String description,MultipartFile image,String actor){
  if(accounts.current(actor).getRole()!=Role.ADMIN)throw new BusinessException("Chỉ quản trị viên được gửi mẫu.");
  var r=requests.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu bánh."));
  editable(r);
  if(description==null||description.isBlank()||description.length()>1500||image==null||image.isEmpty())throw new BusinessException("Chọn ảnh JPG/PNG và mô tả mẫu, tối đa 1.500 ký tự.");
  var d=new CakeDesign();d.setRequest(r);d.setDescription(description.strip());d.setImage(media.saveReviewPhoto(image));d.setActor(actor);designs.saveAndFlush(d);
  r.setUpdatedAt(LocalDateTime.now());
  notices.send(r.getUser(),"Mời duyệt mẫu bánh #TK"+id,"Cửa hàng đã gửi mẫu mới. Kiểm tra màu sắc, lời chúc và chi tiết trước khi duyệt; mẫu mới thay thế lần duyệt trước.","/custom-cakes/"+id);
 }
 @Transactional public void respond(Long id,Long designId,boolean approve,String feedback,String email){
  var r=requests.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu bánh."));
  if(!r.getUser().getId().equals(accounts.current(email).getId()))throw new BusinessException("Bạn không có quyền duyệt mẫu này.");
  editable(r);var d=designs.findTopByRequestIdOrderByIdDesc(id).orElseThrow(()->new BusinessException("Cửa hàng chưa gửi mẫu."));
  if(!d.getId().equals(designId)||d.getStatus()!=CakeDesign.Status.PENDING)throw new BusinessException("Mẫu đã thay đổi hoặc đã được phản hồi. Vui lòng tải lại trang.");
  if(feedback!=null&&feedback.length()>1000)throw new BusinessException("Ý kiến tối đa 1.000 ký tự.");
  if(!approve&&(feedback==null||feedback.isBlank()))throw new BusinessException("Vui lòng mô tả chi tiết cần chỉnh sửa.");
  d.setStatus(approve?CakeDesign.Status.APPROVED:CakeDesign.Status.CHANGES_REQUESTED);d.setFeedback(feedback==null?null:feedback.strip());d.setRespondedAt(LocalDateTime.now());r.setUpdatedAt(LocalDateTime.now());
  notices.admins("Mẫu bánh #TK"+id+": "+d.getStatusLabel(),r.getUser().getName()+" phản hồi mẫu #"+designId+". "+(feedback==null?"":feedback),"/admin/custom-cakes/"+id);
  notices.send(r.getUser(),"Đã lưu phản hồi mẫu #TK"+id,d.getStatusLabel()+". Cửa hàng chỉ bắt đầu làm khi đã chốt báo giá, duyệt mẫu mới nhất và đủ tiền trả trước.","/custom-cakes/"+id);
 }
 private void editable(CustomCakeRequest r){if(r.getStatus()!=CustomCakeStatus.QUOTED&&r.getStatus()!=CustomCakeStatus.ACCEPTED)throw new BusinessException("Chỉ trao đổi mẫu sau khi báo giá và trước khi bắt đầu làm bánh.");}
 public void requireApproved(Long id){if(designs.findTopByRequestIdOrderByIdDesc(id).filter(d->d.getStatus()==CakeDesign.Status.APPROVED).isEmpty())throw new BusinessException("Khách cần duyệt mẫu thiết kế mới nhất trước khi bắt đầu làm bánh.");}
}
