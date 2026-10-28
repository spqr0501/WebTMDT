package com.example.demo.controller;
import com.example.demo.service.*;
import com.example.demo.dto.*;
import com.example.demo.repository.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.*;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.data.domain.PageRequest;

@Controller
public class ProfileController {
 private final AccountService accounts;private final ReviewRepository reviews;private final CustomerOrderRepository orders;
 public ProfileController(AccountService accounts,ReviewRepository reviews,CustomerOrderRepository orders){this.accounts=accounts;this.reviews=reviews;this.orders=orders;}
 private String hub(Principal p,String tab,int page,boolean checkout,Model m){
  if(!java.util.Set.of("profile","addresses","reviews","security").contains(tab))tab="profile";
  var u=accounts.current(p.getName());m.addAttribute("accountUser",u);m.addAttribute("tab",tab);m.addAttribute("fromCheckout",checkout);var addresses=accounts.addresses(p.getName());m.addAttribute("addresses",addresses);m.addAttribute("reviewCount",reviews.countByUserId(u.getId()));m.addAttribute("orderCount",orders.countByUserId(u.getId()));
  if(!m.containsAttribute("profileForm")){var f=new ProfileForm();f.setName(u.getName());f.setPhone(u.getPhone());m.addAttribute("profileForm",f);}
  if(!m.containsAttribute("addressForm")){var f=new AddressForm();f.setRecipient(u.getName());f.setPhone(u.getPhone());m.addAttribute("addressForm",f);}
  if(!m.containsAttribute("passwordForm"))m.addAttribute("passwordForm",new PasswordChangeForm());
  if(tab.equals("reviews")){var result=reviews.findByUserIdOrderByCreatedAtDescIdDesc(u.getId(),PageRequest.of(Math.max(0,page),6));if(result.getTotalPages()>0&&result.getNumber()>=result.getTotalPages())result=reviews.findByUserIdOrderByCreatedAtDescIdDesc(u.getId(),PageRequest.of(result.getTotalPages()-1,6));m.addAttribute("reviewPage",result);m.addAttribute("reviewLinks",PageLinks.of(result.getNumber(),result.getTotalPages()));}
  return "account/profile";
 }
 @GetMapping("/account") public String profile(Principal p,@RequestParam(defaultValue="profile") String tab,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="false") boolean checkout,@RequestParam(required=false) Long addressId,Model m){
  if(tab.equals("addresses")&&addressId!=null){var a=accounts.ownedAddress(p.getName(),addressId);var f=new AddressForm();org.springframework.beans.BeanUtils.copyProperties(a,f);m.addAttribute("addressForm",f);m.addAttribute("addressId",addressId);}
  return hub(p,tab,page,checkout,m);
 }
 @GetMapping("/account/addresses") public String legacy(@RequestParam(defaultValue="false") boolean checkout){return "redirect:/account?tab=addresses"+(checkout?"&checkout=true":"");}
 @PostMapping("/account/profile") public String save(Principal p,@Valid @ModelAttribute("profileForm") ProfileForm f,BindingResult errors,Model m,RedirectAttributes flash){if(errors.hasErrors())return hub(p,"profile",0,false,m);accounts.updateProfile(p.getName(),f);flash.addFlashAttribute("success","Đã cập nhật thông tin cá nhân. Thông tin trên các đơn cũ được giữ nguyên.");return "redirect:/account";}
 @PostMapping("/account/addresses") public String address(Principal p,@Valid @ModelAttribute("addressForm") AddressForm f,BindingResult errors,@RequestParam(required=false) Long addressId,@RequestParam(defaultValue="false") boolean checkout,Model m,RedirectAttributes flash){
  m.addAttribute("addressId",addressId);if(errors.hasErrors())return hub(p,"addresses",0,checkout,m);
  try{if(addressId==null)accounts.addAddress(p.getName(),f);else accounts.saveAddress(p.getName(),addressId,f);}catch(BusinessException e){errors.reject("address",e.getMessage());return hub(p,"addresses",0,checkout,m);}
  flash.addFlashAttribute("success","Đã lưu địa chỉ giao bánh.");return checkout?"redirect:/checkout":"redirect:/account?tab=addresses";
 }
 @PostMapping("/account/addresses/{id}/delete") public String delete(Principal p,@PathVariable Long id,RedirectAttributes flash){accounts.deleteAddress(p.getName(),id);flash.addFlashAttribute("success","Đã xóa địa chỉ khỏi sổ địa chỉ. Địa chỉ trên đơn đã đặt được giữ nguyên.");return "redirect:/account?tab=addresses";}
 @PostMapping("/account/password") public String password(Principal p,@Valid @ModelAttribute("passwordForm") PasswordChangeForm f,BindingResult errors,Model m,HttpServletRequest request,HttpServletResponse response,Authentication auth){
  if(errors.hasErrors())return hub(p,"security",0,false,m);
  try{accounts.changePassword(p.getName(),f);}catch(BusinessException e){errors.reject("password",e.getMessage());return hub(p,"security",0,false,m);}
  new SecurityContextLogoutHandler().logout(request,response,auth);return "redirect:/login?passwordChanged";
 }
}
