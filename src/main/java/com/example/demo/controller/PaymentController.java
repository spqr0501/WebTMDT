package com.example.demo.controller;
import com.example.demo.dto.PaymentRecordForm;
import com.example.demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import java.security.Principal;
@Controller
public class PaymentController {
 private final PaymentService service;
 public PaymentController(PaymentService service){this.service=service;}
 @PostMapping("/admin/orders/{id}/payments") public String order(@PathVariable Long id,@Valid @ModelAttribute PaymentRecordForm form,BindingResult errors,Principal principal,RedirectAttributes flash){return save(id,false,form,errors,principal,flash);}
 @PostMapping("/admin/custom-cakes/{id}/payments") public String custom(@PathVariable Long id,@Valid @ModelAttribute PaymentRecordForm form,BindingResult errors,Principal principal,RedirectAttributes flash){return save(id,true,form,errors,principal,flash);}
 private String save(Long id,boolean custom,PaymentRecordForm form,BindingResult errors,Principal principal,RedirectAttributes flash){
  if(errors.hasErrors())flash.addFlashAttribute("paymentError","Nhập số tiền hợp lệ, tham chiếu và tải lại trang nếu mã giao dịch đã hết.");
  else try{if(custom)service.recordCustom(id,form,principal.getName());else service.recordOrder(id,form,principal.getName());flash.addFlashAttribute("success","Đã ghi nhận giao dịch. Vui lòng kiểm tra tiền thực tế trước khi cập nhật trạng thái đơn.");}catch(BusinessException e){flash.addFlashAttribute("paymentError",e.getMessage());}
  return "redirect:"+(custom?"/admin/custom-cakes/":"/admin/orders/")+id;
 }
}
