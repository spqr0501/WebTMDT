package com.example.demo.controller;
import com.example.demo.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.security.Principal;
@Controller
public class AssistantController {
 private final AssistantService assistant;private final StorePolicyService policies;
 public AssistantController(AssistantService assistant,StorePolicyService policies){this.assistant=assistant;this.policies=policies;}
 @ModelAttribute("examples") public java.util.List<String> examples(){return java.util.List.of("Gợi ý bánh kem dưới 500k","Nhận tại cửa hàng cần cọc bao nhiêu?","Điều kiện đổi trả và hoàn tiền","Tôi muốn gặp nhân viên");}
 public static AssistantService.Conversation conversation(HttpSession session,String email){
  synchronized(session){if(!email.equals(session.getAttribute("assistantOwner"))||!(session.getAttribute("assistantConversation") instanceof AssistantService.Conversation)){session.setAttribute("assistantOwner",email);session.setAttribute("assistantConversation",new AssistantService.Conversation());}return (AssistantService.Conversation)session.getAttribute("assistantConversation");}
 }
 @GetMapping("/assistant") public String page(HttpSession session,Principal p,Model m){var c=conversation(session,p.getName());synchronized(c){m.addAttribute("turns",java.util.List.copyOf(c.turns));}return "support/assistant";}
 @GetMapping("/assistant/history") @ResponseBody public org.springframework.http.ResponseEntity<java.util.List<AssistantService.Turn>> history(HttpSession session,Principal p){var c=conversation(session,p.getName());synchronized(c){return org.springframework.http.ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore()).body(java.util.List.copyOf(c.turns));}}
 @PostMapping("/assistant/message") @ResponseBody public org.springframework.http.ResponseEntity<AssistantService.Answer> message(@RequestParam String question,HttpSession session,Principal p){var c=conversation(session,p.getName());synchronized(c){return org.springframework.http.ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore()).body(assistant.ask(question,c));}}
 @PostMapping("/assistant/ask") public String ask(@RequestParam String question,HttpSession session,Principal p,Model m){var c=conversation(session,p.getName());synchronized(c){try{assistant.ask(question,c);}catch(BusinessException e){m.addAttribute("chatError",e.getMessage());}m.addAttribute("turns",java.util.List.copyOf(c.turns));}return "support/assistant";}
 @PostMapping("/assistant/reset") public String reset(HttpSession session){session.removeAttribute("assistantConversation");return "redirect:/assistant";}
 @GetMapping("/policies") public String policies(Model m){m.addAttribute("policies",policies.policies());return "shop/policies";}
}
