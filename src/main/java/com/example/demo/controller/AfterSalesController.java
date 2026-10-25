package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.dto.PaymentRecordForm;
import com.example.demo.repository.*;
import com.example.demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.*;
import java.security.Principal;
import java.math.BigDecimal;
import java.util.*;

@Controller
public class AfterSalesController {
 @org.springframework.beans.factory.annotation.Autowired private MediaService media;
 @GetMapping("/after-sales/{id}/photos/{index}") @ResponseBody public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> photo(@PathVariable Long id,@PathVariable int index,Principal p){return evidence(service.owned(id,p.getName()),index);}
 @GetMapping("/admin/after-sales/{id}/photos/{index}") @ResponseBody public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> adminPhoto(@PathVariable Long id,@PathVariable int index){return evidence(cases.findById(id).orElseThrow(),index);}
 private org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> evidence(AfterSalesRequest c,int index){if(index<0||index>=c.getPhotos().size())throw new BusinessException("Không tìm thấy ảnh minh chứng.");return org.springframework.http.ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore()).contentType(org.springframework.http.MediaType.IMAGE_PNG).body(new org.springframework.core.io.FileSystemResource(media.resolvePrivate(c.getPhotos().get(index))));}
 private final AfterSalesService service;private final AfterSalesRepository cases;private final AccountService accounts;private final OrderService orders;private final CustomCakeService custom;private final PaymentService payments;private final PaymentRecordRepository records;
 public AfterSalesController(AfterSalesService service,AfterSalesRepository cases,AccountService accounts,OrderService orders,CustomCakeService custom,PaymentService payments,PaymentRecordRepository records){this.service=service;this.cases=cases;this.accounts=accounts;this.orders=orders;this.custom=custom;this.payments=payments;this.records=records;}
 private void parent(Long orderId,Long customId,Principal p,Model m){if((orderId==null)==(customId==null))throw new BusinessException("Chọn đúng một đơn cần hỗ trợ.");m.addAttribute("orderId",orderId);m.addAttribute("customId",customId);m.addAttribute("parentCode",orderId!=null?"#"+orders.owned(p.getName(),orderId).getId():"#TK"+custom.owned(p.getName(),customId).getId());}
 @GetMapping("/after-sales/new") public String form(@RequestParam(required=false) Long orderId,@RequestParam(required=false) Long customId,Principal p,Model m){parent(orderId,customId,p,m);return "after-sales/form";}
 @PostMapping("/after-sales") public String create(@RequestParam(required=false) Long orderId,@RequestParam(required=false) Long customId,@RequestParam String reason,@RequestParam AfterSalesRequest.Resolution resolution,@RequestParam(required=false) List<MultipartFile> photos,Principal p,Model m){parent(orderId,customId,p,m);try{var c=service.submit(orderId!=null?orderId:customId,customId!=null,p.getName(),reason,resolution,photos);return "redirect:/after-sales/"+c.getId();}catch(BusinessException e){m.addAttribute("formError",e.getMessage());m.addAttribute("reason",reason);return "after-sales/form";}}
 @GetMapping("/after-sales") public String list(@RequestParam(defaultValue="0") int page,Principal p,Model m){m.addAttribute("page",cases.findByUserIdOrderByCreatedAtDesc(accounts.current(p.getName()).getId(),PageRequest.of(Math.max(page,0),15)));m.addAttribute("isAdmin",false);return "after-sales/list";}
 @GetMapping("/admin/after-sales") public String adminList(@RequestParam(defaultValue="0") int page,@RequestParam(required=false) AfterSalesRequest.Status status,Model m){var pageable=PageRequest.of(Math.max(page,0),15,Sort.by("createdAt").descending());m.addAttribute("page",status==null?cases.findAll(pageable):cases.findByStatus(status,pageable));m.addAttribute("isAdmin",true);m.addAttribute("status",status);return "admin/after-sales-list";}
 private void detail(AfterSalesRequest c,boolean admin,Model m){m.addAttribute("case",c);m.addAttribute("isAdmin",admin);m.addAttribute("paymentToken",UUID.randomUUID().toString());m.addAttribute("refundRecords",records.findByAfterSalesIdOrderByCreatedAtAscIdAsc(c.getId()));}
 @GetMapping("/after-sales/{id}") public String detail(@PathVariable Long id,Principal p,Model m){detail(service.owned(id,p.getName()),false,m);return "after-sales/detail";}
 @GetMapping("/admin/after-sales/{id}") public String adminDetail(@PathVariable Long id,Model m){detail(cases.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu.")),true,m);return "admin/after-sales-detail";}
 @PostMapping("/admin/after-sales/{id}/decision") public String decide(@PathVariable Long id,@RequestParam boolean approve,@RequestParam(required=false) AfterSalesRequest.Resolution resolution,@RequestParam(required=false) BigDecimal amount,@RequestParam String reply,Principal p,RedirectAttributes f){try{service.decide(id,approve,resolution,amount,reply,p.getName());f.addFlashAttribute("success","Đã lưu kết quả và thông báo cho khách.");}catch(BusinessException e){f.addFlashAttribute("formError",e.getMessage());}return "redirect:/admin/after-sales/"+id;}
 @PostMapping("/admin/after-sales/{id}/replacement") public String complete(@PathVariable Long id,@RequestParam String reply,Principal p,RedirectAttributes f){try{service.completeReplacement(id,reply,p.getName());f.addFlashAttribute("success","Đã xác nhận đổi bánh / làm lại xong.");}catch(BusinessException e){f.addFlashAttribute("formError",e.getMessage());}return "redirect:/admin/after-sales/"+id;}
 @PostMapping("/admin/after-sales/{id}/refund") public String refund(@PathVariable Long id,@RequestParam BigDecimal amount,@RequestParam String reference,@RequestParam String token,Principal p,RedirectAttributes f){try{var form=new PaymentRecordForm();form.setKind(PaymentRecord.Kind.REFUND);form.setAmount(amount);form.setReference(reference);form.setToken(token);payments.refundCase(id,form,p.getName());f.addFlashAttribute("success","Đã ghi nhận khoản tiền đã hoàn thực tế.");}catch(BusinessException e){f.addFlashAttribute("formError",e.getMessage());}return "redirect:/admin/after-sales/"+id;}
}
