package com.example.demo.controller;
import com.example.demo.service.*;
import com.example.demo.repository.StripePaymentRepository;
import com.example.demo.entity.StripePayment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.http.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
@Controller
public class StripePaymentController {
 private final StripePaymentService payments;private final StripePaymentRepository attempts;
 public StripePaymentController(StripePaymentService payments,StripePaymentRepository attempts){this.payments=payments;this.attempts=attempts;}
 @PostMapping("/payments/stripe/start") public ResponseEntity<?> start(Principal p,@RequestParam(required=false) Long orderId,@RequestParam(required=false) Long customId){String url=payments.start(p.getName(),orderId,customId);return ResponseEntity.status(303).cacheControl(CacheControl.noStore()).location(java.net.URI.create(url)).build();}
 @GetMapping("/payments/stripe/{id}") public String result(@PathVariable String id,Principal p,Model m){m.addAttribute("payment",payments.owned(p.getName(),id));return "payments/stripe-result";}
 @PostMapping("/payments/stripe/{id}/close") public String close(@PathVariable String id,Principal p,RedirectAttributes f){try{payments.close(p.getName(),id);f.addFlashAttribute("success","Đã đóng phiên Stripe cũ. Bạn có thể tạo phiên mới từ đơn nếu còn khoản cần trả.");}catch(BusinessException e){f.addFlashAttribute("warning",e.getMessage());}return "redirect:/payments/stripe/"+id;}
 @GetMapping("/payments/stripe/{id}/state") @ResponseBody public ResponseEntity<?> state(@PathVariable String id,Principal p){var a=payments.owned(p.getName(),id);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(java.util.Map.of("status",a.getStatus().name(),"label",a.getLabel(),"final",a.getStatus()!=StripePayment.Status.CREATING&&a.getStatus()!=StripePayment.Status.OPEN));}
 @GetMapping("/admin/stripe-payments") public String admin(@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("page",attempts.findAllByOrderByCreatedAtDesc(org.springframework.data.domain.PageRequest.of(Math.max(0,page),20)));return "admin/stripe-payments";}
 @PostMapping("/admin/stripe-payments/{id}/resolve") public String resolve(@PathVariable String id,@RequestParam String note,@RequestParam(defaultValue="false") boolean confirmed,Principal p,RedirectAttributes f){try{payments.resolve(p.getName(),id,note,confirmed);f.addFlashAttribute("success","Đã lưu đối soát thủ công; không tự tạo khoản thu/hoàn.");}catch(BusinessException e){f.addFlashAttribute("warning",e.getMessage());}return "redirect:/admin/stripe-payments";}
}
