package com.example.demo.controller;
import com.example.demo.service.*;
import com.example.demo.dto.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import java.security.Principal;
@Controller
public class AccountController {
 private final AccountService accounts;
 public AccountController(AccountService accounts){this.accounts=accounts;}
 @GetMapping("/login") public String login(){return "auth/login";}
 @GetMapping("/register") public String register(Model m){m.addAttribute("form",new RegistrationForm());return "auth/register";}
 @PostMapping("/register") public String register(@Valid @ModelAttribute("form") RegistrationForm form,BindingResult errors){
  if(!errors.hasFieldErrors("password")&&!errors.hasFieldErrors("confirmPassword")&&!java.util.Objects.equals(form.getPassword(),form.getConfirmPassword()))errors.rejectValue("confirmPassword","password.mismatch","Mật khẩu nhập lại chưa khớp.");
  if(errors.hasErrors())return "auth/register";
  try{accounts.register(form);}catch(BusinessException e){errors.reject("registration",e.getMessage());return "auth/register";}
  return "redirect:/login?registered";
 }
}
