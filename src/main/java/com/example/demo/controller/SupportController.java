package com.example.demo.controller;
import com.example.demo.entity.SupportTicket;
import com.example.demo.repository.*;
import com.example.demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.data.domain.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.*;
@Controller
public class SupportController {
 private final SupportService support;private final SupportTicketRepository tickets;private final SupportMessageRepository messages;private final AccountService accounts;private final AssistantService assistant;
 public SupportController(SupportService support,SupportTicketRepository tickets,SupportMessageRepository messages,AccountService accounts,AssistantService assistant){this.support=support;this.tickets=tickets;this.messages=messages;this.accounts=accounts;this.assistant=assistant;}
 private void token(HttpSession s,Model m){if(s.getAttribute("supportToken")==null)s.setAttribute("supportToken",UUID.randomUUID().toString());m.addAttribute("supportToken",s.getAttribute("supportToken"));}
 @GetMapping("/support/new") public String form(HttpSession session,Model m){token(session,m);return "support/form";}
 @PostMapping("/support") public String create(@RequestParam String subject,@RequestParam String content,@RequestParam String token,@RequestParam(defaultValue="false") boolean shareConversation,HttpSession session,Principal p,Model m){
  if(!token.equals(session.getAttribute("supportToken")))throw new BusinessException("Phiên gửi phiếu đã kết thúc. Vui lòng kiểm tra danh sách phiếu trước khi gửi lại.");
  String transcript=null;if(shareConversation){var c=AssistantController.conversation(session,p.getName());synchronized(c){transcript=assistant.transcript(c);}}
  try{var t=support.create(p.getName(),subject,content,token,transcript);session.removeAttribute("supportToken");return "redirect:/support/"+t.getId();}catch(BusinessException e){m.addAttribute("formError",e.getMessage());m.addAttribute("subject",subject);m.addAttribute("content",content);token(session,m);return "support/form";}
 }
 @GetMapping("/support") public String list(@RequestParam(defaultValue="0") int page,Principal p,Model m){m.addAttribute("page",tickets.findByUserIdOrderByUpdatedAtDesc(accounts.current(p.getName()).getId(),PageRequest.of(Math.max(page,0),15)));m.addAttribute("isAdmin",false);return "support/list";}
 @GetMapping("/admin/support") public String adminList(@RequestParam(defaultValue="0") int page,@RequestParam(required=false) SupportTicket.Status status,Model m){var pageable=PageRequest.of(Math.max(page,0),15,Sort.by("updatedAt").descending());m.addAttribute("page",status==null?tickets.findAll(pageable):tickets.findByStatus(status,pageable));m.addAttribute("isAdmin",true);m.addAttribute("status",status);return "admin/support-list";}
 private void detail(SupportTicket ticket,boolean admin,int page,Model m){m.addAttribute("ticket",ticket);m.addAttribute("isAdmin",admin);m.addAttribute("messages",messages.findByTicketIdOrderByIdAsc(ticket.getId(),PageRequest.of(Math.max(page,0),30)));}
 @GetMapping("/support/{id}") public String detail(@PathVariable Long id,@RequestParam(defaultValue="0") int page,Principal p,Model m){detail(support.owned(id,p.getName()),false,page,m);return "support/detail";}
 @GetMapping("/admin/support/{id}") public String adminDetail(@PathVariable Long id,@RequestParam(defaultValue="0") int page,Model m){detail(tickets.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy phiếu.")),true,page,m);return "admin/support-detail";}
 @PostMapping("/support/{id}/reply") public String reply(@PathVariable Long id,@RequestParam String content,Principal p,RedirectAttributes f){try{support.reply(id,p.getName(),content,false,null);f.addFlashAttribute("success","Đã gửi bổ sung cho nhân viên.");}catch(BusinessException e){f.addFlashAttribute("formError",e.getMessage());}return "redirect:/support/"+id;}
 @PostMapping("/admin/support/{id}/reply") public String adminReply(@PathVariable Long id,@RequestParam String content,@RequestParam SupportTicket.Status status,Principal p,RedirectAttributes f){try{support.reply(id,p.getName(),content,true,status);f.addFlashAttribute("success","Đã trả lời và thông báo cho khách.");}catch(BusinessException e){f.addFlashAttribute("formError",e.getMessage());}return "redirect:/admin/support/"+id;}
}
