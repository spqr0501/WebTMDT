package com.example.demo.controller;
import com.example.demo.dto.ReviewForm;
import com.example.demo.service.*;
import com.example.demo.repository.ReviewRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.data.domain.*;
import jakarta.validation.Valid;
import java.security.Principal;
@Controller
public class ReviewController {
 private final ReviewService service;private final ReviewRepository reviews;
 public ReviewController(ReviewService service,ReviewRepository reviews){this.service=service;this.reviews=reviews;}
 @PostMapping("/reviews/{productId}") public String submit(@PathVariable Long productId,@Valid @ModelAttribute ReviewForm form,BindingResult errors,@RequestParam(required=false) MultipartFile[] photos,Principal p,RedirectAttributes flash){
  if(errors.hasErrors()){flash.addFlashAttribute("reviewError","Chọn từ 1–5 sao và viết nhận xét từ 10 đến 2.000 ký tự.");flash.addFlashAttribute("reviewForm",form);}
  else try{service.submit(p.getName(),productId,form,photos);flash.addFlashAttribute("success","Cảm ơn bạn! Đánh giá của bạn đã được đăng.");}
  catch(BusinessException e){flash.addFlashAttribute("reviewError",e.getMessage());flash.addFlashAttribute("reviewForm",form);}
  return "redirect:/products/"+productId+"#reviews";
 }
 @GetMapping("/admin/reviews") public String list(@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("page",reviews.findAll(PageRequest.of(Math.max(0,page),15,Sort.by("createdAt").descending())));return "admin/reviews";}
 @PostMapping("/admin/reviews/{id}/visibility") public String moderate(@PathVariable Long id,@RequestParam boolean visible,@RequestParam(defaultValue="") String reason,RedirectAttributes flash){service.moderate(id,visible,reason);flash.addFlashAttribute("success","Đã cập nhật hiển thị đánh giá.");return "redirect:/admin/reviews";}
}
