package com.example.demo.controller;
import com.example.demo.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.security.Principal;
@Controller
public class NotificationController {
 private final NotificationService notices;
 public NotificationController(NotificationService notices){this.notices=notices;}
 @GetMapping("/notifications") public String page(Principal p,@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("page",notices.page(p.getName(),page));return "account/notifications";}
 @GetMapping("/notifications/count") @ResponseBody public java.util.Map<String,Long> count(Principal p){return java.util.Map.of("unread",notices.unread(p.getName()));}
 @PostMapping("/notifications/{id}/open") public String open(Principal p,@PathVariable Long id){return "redirect:"+notices.open(p.getName(),id);}
 @PostMapping("/notifications/read-all") public String readAll(Principal p){notices.readAll(p.getName());return "redirect:/notifications";}
}
