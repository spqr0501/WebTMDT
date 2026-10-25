package com.example.demo.controller;
import com.example.demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import jakarta.servlet.http.*;
@Controller
public class PasswordResetController {
 private final PasswordResetService service;
 public PasswordResetController(PasswordResetService service){this.service=service;}
 private void protect(HttpServletResponse response){response.setHeader("Cache-Control","no-store");response.setHeader("Referrer-Policy","no-referrer");}
 @GetMapping("/forgot-password") public String forgot(Model m,HttpServletResponse response){protect(response);m.addAttribute("mailReady",service.ready());return "auth/forgot-password";}
 @PostMapping("/forgot-password") public String send(@RequestParam String email,HttpServletRequest req,HttpServletResponse response,Model m){protect(response);m.addAttribute("mailReady",service.ready());try{if(email.length()>190||!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw new BusinessException("Vui lòng nhập email hợp lệ.");service.request(email,req.getRemoteAddr());m.addAttribute("success","Nếu email này thuộc tài khoản đang hoạt động, hướng dẫn sẽ được gửi tới hộp thư. Vui lòng kiểm tra cả thư rác và chờ ít nhất một phút trước khi yêu cầu lại.");}catch(BusinessException e){m.addAttribute("formError",e.getMessage());}return "auth/forgot-password";}
 @GetMapping("/reset-password") public String reset(@RequestParam(defaultValue="") String token,HttpServletResponse response,Model m){protect(response);m.addAttribute("token",token);m.addAttribute("validToken",service.valid(token));return "auth/reset-password";}
 @PostMapping("/reset-password") public String update(@RequestParam String token,@RequestParam String password,@RequestParam String confirmPassword,HttpServletRequest request,HttpServletResponse response,Model m){protect(response);try{service.reset(token,password,confirmPassword);var session=request.getSession(false);if(session!=null)session.invalidate();org.springframework.security.core.context.SecurityContextHolder.clearContext();return "redirect:/login?reset";}catch(BusinessException e){m.addAttribute("formError",e.getMessage());m.addAttribute("token",token);m.addAttribute("validToken",service.valid(token));return "auth/reset-password";}}
}
