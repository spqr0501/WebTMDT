package com.example.demo.controller;

import com.example.demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
public class PickupController {
 private final PickupService pickups;private final OrderService orders;private final CustomCakeService custom;
 public PickupController(PickupService pickups,OrderService orders,CustomCakeService custom){this.pickups=pickups;this.orders=orders;this.custom=custom;}
 @GetMapping(value="/orders/{id}/pickup.png",produces=MediaType.IMAGE_PNG_VALUE)
 @ResponseBody public ResponseEntity<byte[]> orderQr(@PathVariable Long id,Principal p){return image(pickups.orderCode(orders.owned(p.getName(),id)));}
 @GetMapping(value="/custom-cakes/{id}/pickup.png",produces=MediaType.IMAGE_PNG_VALUE)
 @ResponseBody public ResponseEntity<byte[]> customQr(@PathVariable Long id,Principal p){return image(pickups.customCode(custom.owned(p.getName(),id)));}
 private ResponseEntity<byte[]> image(String code){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.IMAGE_PNG).body(pickups.png(code));}
 @GetMapping("/admin/pickup") public String page(){return "admin/pickup";}
 @PostMapping("/admin/pickup/lookup") public String lookup(@RequestParam(defaultValue="") String code,@RequestParam(required=false) MultipartFile image,Model m){try{m.addAttribute("ticket",pickups.lookup(image!=null&&!image.isEmpty()?pickups.read(image):code));}catch(BusinessException e){m.addAttribute("pickupError",e.getMessage());}return "admin/pickup";}
 @PostMapping("/admin/pickup/complete") public String complete(@RequestParam String code,Principal p,RedirectAttributes flash){var ticket=pickups.complete(code,p.getName());flash.addFlashAttribute("success","Đã xác nhận bàn giao bánh và gửi thông báo cho khách.");return "redirect:"+ticket.detailUrl();}
}
