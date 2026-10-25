package com.example.demo.controller;
import com.example.demo.dto.CustomCakeForm;
import com.example.demo.entity.*;
import com.example.demo.repository.CustomCakeRequestRepository;
import com.example.demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.data.domain.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.security.Principal;
import java.math.BigDecimal;
@Controller
public class CustomCakeController {
 private final CakeDesignService designs;private final PickupService pickups;private final PaymentService payments;private final CustomCakeService service;private final CustomCakeRequestRepository requests;private final AccountService accounts;
 @org.springframework.beans.factory.annotation.Value("${app.bank.instructions}") private String bank;
 public CustomCakeController(CustomCakeService service,CustomCakeRequestRepository requests,AccountService accounts,PaymentService payments,CakeDesignService designs,PickupService pickups){this.designs=designs;this.pickups=pickups;this.payments=payments;this.service=service;this.requests=requests;this.accounts=accounts;}
 private void dates(Model m){m.addAttribute("minDate",LocalDate.now().plusDays(3));m.addAttribute("maxDate",LocalDate.now().plusDays(90));}
 @GetMapping("/custom-cakes/new") public String form(Principal p,HttpSession session,Model m){if(session.getAttribute("customCakeToken")==null)session.setAttribute("customCakeToken",java.util.UUID.randomUUID().toString());CustomCakeForm f=new CustomCakeForm();f.setToken((String)session.getAttribute("customCakeToken"));f.setRecipient(accounts.current(p.getName()).getName());f.setReceiveDate(LocalDate.now().plusDays(3));m.addAttribute("form",f);dates(m);return "custom/form";}
 @PostMapping("/custom-cakes") public String submit(Principal p,HttpSession session,@Valid @ModelAttribute("form") CustomCakeForm f,BindingResult errors,@RequestParam(required=false) MultipartFile referenceImage,Model m){
  if(f.getToken()==null||!f.getToken().equals(session.getAttribute("customCakeToken")))throw new BusinessException("Phiên gửi yêu cầu đã kết thúc. Vui lòng xem lại danh sách yêu cầu.");
  if(errors.hasErrors()){dates(m);return "custom/form";}
  try{var r=service.submit(p.getName(),f,referenceImage);session.removeAttribute("customCakeToken");return "redirect:/custom-cakes/"+r.getId();}catch(BusinessException e){errors.reject("custom",e.getMessage());dates(m);return "custom/form";}
 }
 @GetMapping("/custom-cakes") public String list(Principal p,@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("page",requests.findByUserIdOrderByCreatedAtDesc(accounts.current(p.getName()).getId(),PageRequest.of(Math.max(0,page),15)));m.addAttribute("isAdmin",false);return "custom/list";}
 @GetMapping("/custom-cakes/{id}") public String detail(Principal p,@PathVariable Long id,Model m){var request=service.owned(p.getName(),id);m.addAttribute("request",request);m.addAttribute("designs",designs.history(id));if(request.getFulfillmentMethod()==FulfillmentMethod.PICKUP)m.addAttribute("pickupCode",pickups.customCode(request));m.addAttribute("paymentRecords",payments.customHistory(id));m.addAttribute("paymentToken",java.util.UUID.randomUUID().toString());m.addAttribute("bank",bank);m.addAttribute("isAdmin",false);return "custom/detail";}
 @PostMapping("/custom-cakes/{id}/action") public String action(Principal p,@PathVariable Long id,@RequestParam boolean accept,RedirectAttributes flash){service.customerAction(p.getName(),id,accept);flash.addFlashAttribute("success",accept?"Đã xác nhận báo giá. Cửa hàng sẽ chuẩn bị bánh theo yêu cầu.":"Đã hủy yêu cầu.");return "redirect:/custom-cakes/"+id;}
 @GetMapping("/admin/custom-cakes") public String admin(@RequestParam(defaultValue="0") int page,@RequestParam(required=false) CustomCakeStatus status,Model m){var pageable=PageRequest.of(Math.max(0,page),15,Sort.by("createdAt").descending());m.addAttribute("page",status==null?requests.findAll(pageable):requests.findByStatus(status,pageable));m.addAttribute("statuses",CustomCakeStatus.values());m.addAttribute("status",status);m.addAttribute("isAdmin",true);return "admin/custom-list";}
 @GetMapping("/admin/custom-cakes/{id}") public String adminDetail(@PathVariable Long id,Model m){m.addAttribute("request",requests.findById(id).orElseThrow());m.addAttribute("designs",designs.history(id));m.addAttribute("paymentRecords",payments.customHistory(id));m.addAttribute("paymentToken",java.util.UUID.randomUUID().toString());m.addAttribute("bank",bank);m.addAttribute("isAdmin",true);return "admin/custom-detail";}
 @PostMapping("/admin/custom-cakes/{id}/designs") public String propose(@PathVariable Long id,@RequestParam String description,@RequestParam MultipartFile image,Principal p,RedirectAttributes flash){designs.propose(id,description,image,p.getName());flash.addFlashAttribute("success","Đã gửi mẫu mới cho khách duyệt.");return "redirect:/admin/custom-cakes/"+id;}
 @PostMapping("/custom-cakes/{id}/designs/{designId}") public String respond(@PathVariable Long id,@PathVariable Long designId,@RequestParam boolean approve,@RequestParam(defaultValue="") String feedback,Principal p,RedirectAttributes flash){designs.respond(id,designId,approve,feedback,p.getName());flash.addFlashAttribute("success","Đã lưu phản hồi mẫu và thông báo cho cửa hàng.");return "redirect:/custom-cakes/"+id;}
 @PostMapping("/admin/custom-cakes/{id}/status") public String update(@PathVariable Long id,@RequestParam CustomCakeStatus status,@RequestParam(required=false) BigDecimal quotedTotal,@RequestParam(defaultValue="") String reply,RedirectAttributes flash){service.update(id,status,quotedTotal,reply);flash.addFlashAttribute("success","Đã cập nhật và gửi thông báo cho khách.");return "redirect:/admin/custom-cakes/"+id;}
}
