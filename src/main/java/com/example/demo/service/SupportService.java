package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class SupportService {
 private final SupportTicketRepository tickets;private final SupportMessageRepository messages;private final UserRepository users;private final AccountService accounts;private final NotificationService notices;
 public SupportService(SupportTicketRepository tickets,SupportMessageRepository messages,UserRepository users,AccountService accounts,NotificationService notices){this.tickets=tickets;this.messages=messages;this.users=users;this.accounts=accounts;this.notices=notices;}
 public SupportTicket owned(Long id,String email){return tickets.findByIdAndUserId(id,accounts.current(email).getId()).orElseThrow(()->new BusinessException("Không tìm thấy phiếu hỗ trợ của bạn."));}
 @Transactional public SupportTicket create(String email,String subject,String text,String token,String sharedTranscript){
  var user=users.lockById(accounts.current(email).getId()).orElseThrow();if(token==null||!token.matches("[a-fA-F0-9-]{36}"))throw new BusinessException("Tải lại biểu mẫu hỗ trợ.");var prior=tickets.findByTokenAndUserId(token,user.getId());if(prior.isPresent())return prior.get();
  if(subject==null||subject.strip().length()<5||subject.length()>150)throw new BusinessException("Chủ đề từ 5 đến 150 ký tự.");validText(text);
  if(sharedTranscript!=null&&sharedTranscript.length()>6000)throw new BusinessException("Nội dung chia sẻ quá dài.");
  var t=new SupportTicket();t.setUser(user);t.setSubject(subject.strip());t.setToken(token);t.setTranscript(sharedTranscript);tickets.saveAndFlush(t);message(t,text,false,email);
  notices.admins("Phiếu hỗ trợ mới #"+t.getId(),user.getName()+": "+t.getSubject(),"/admin/support/"+t.getId());notices.send(user,"Đã gửi phiếu hỗ trợ #"+t.getId(),"Phiếu đang chờ nhân viên tiếp nhận. Đây là hộp thư trên website, không phải cuộc trò chuyện trực tiếp.","/support/"+t.getId());return t;
 }
 @Transactional public void reply(Long id,String email,String content,boolean staff,SupportTicket.Status status){
  var actor=accounts.current(email);var t=tickets.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy phiếu hỗ trợ."));
  if(staff){if(actor.getRole()!=Role.ADMIN)throw new BusinessException("Bạn không có quyền trả lời thay cửa hàng.");if(status==null)throw new BusinessException("Chọn trạng thái xử lý.");t.setAssignee(email);t.setStatus(status);}
  else{if(!t.getUser().getId().equals(actor.getId()))throw new BusinessException("Bạn không có quyền xem phiếu này.");t.setStatus(SupportTicket.Status.OPEN);}
  validText(content);message(t,content,staff,email);t.setUpdatedAt(java.time.LocalDateTime.now());
  if(staff)notices.send(t.getUser(),"Cửa hàng trả lời phiếu #"+id,content,"/support/"+id);
  else notices.admins("Khách bổ sung phiếu #"+id,t.getUser().getName()+": "+content,"/admin/support/"+id);
 }
 private void validText(String text){if(text==null||text.strip().length()<5||text.length()>2000)throw new BusinessException("Nội dung từ 5 đến 2.000 ký tự.");}
 private void message(SupportTicket t,String text,boolean staff,String actor){var m=new SupportMessage();m.setTicket(t);m.setContent(text.strip());m.setStaff(staff);m.setActor(actor);messages.save(m);}
}
