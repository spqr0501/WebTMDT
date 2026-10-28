package com.example.demo.controller;
import com.example.demo.service.*;
import com.example.demo.dto.ShipperForm;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
public class DeliveryController {
 private final DeliveryService delivery;
 public DeliveryController(DeliveryService delivery){this.delivery=delivery;}
 @GetMapping("/admin/shippers") public String drivers(@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("page",delivery.allDrivers(page));return "admin/shippers";}
 @GetMapping("/admin/shippers/new") public String add(Model m){m.addAttribute("form",new ShipperForm());return "admin/shipper-form";}
 @GetMapping("/admin/shippers/{id}/edit") public String edit(@PathVariable Long id,Model m){var u=delivery.driver(id);var f=new ShipperForm();BeanUtils.copyProperties(u,f,"password");f.setPhone(u.getShipperPhone());f.setVehicle(u.getShipperVehicle());m.addAttribute("form",f);return "admin/shipper-form";}
 @PostMapping("/admin/shippers/save") public String save(@Valid @ModelAttribute("form") ShipperForm form,BindingResult errors,Principal p){if(errors.hasErrors()){form.setPassword(null);return "admin/shipper-form";}try{delivery.save(form,p.getName());}catch(BusinessException e){form.setPassword(null);errors.reject("shipper",e.getMessage());return "admin/shipper-form";}return "redirect:/admin/shippers";}
 @PostMapping("/admin/orders/{id}/shipper") public String assign(@PathVariable Long id,@RequestParam(required=false) Long shipperId,Principal p,RedirectAttributes f){try{delivery.assign(id,shipperId,p.getName());f.addFlashAttribute("success","Đã cập nhật phân công giao hàng.");}catch(BusinessException e){f.addFlashAttribute("deliveryError",e.getMessage());}return "redirect:/admin/orders/"+id;}
 @GetMapping("/shipper") public String jobs(@RequestParam(defaultValue="0") int page,Principal p,Model m){m.addAttribute("page",delivery.jobs(p.getName(),page));return "shipper/jobs";}
 @GetMapping("/shipper/orders/{id}") public String job(@PathVariable Long id,Principal p,Model m){m.addAttribute("order",delivery.assigned(p.getName(),id));return "shipper/order";}
 @PostMapping("/shipper/orders/{id}/{action:start|delivered|issue}") public String action(@PathVariable Long id,@PathVariable String action,@RequestParam(required=false) String message,Principal p,RedirectAttributes f){try{switch(action){case "start"->delivery.start(p.getName(),id);case "delivered"->delivery.delivered(p.getName(),id);case "issue"->delivery.issue(p.getName(),id,message);}f.addFlashAttribute("success","Đã cập nhật tình trạng giao bánh.");}catch(BusinessException e){f.addFlashAttribute("deliveryError",e.getMessage());}return "redirect:/shipper/orders/"+id;}
 @GetMapping("/orders/{id}/tracking") public String tracking(@PathVariable Long id,Principal p,Model m){m.addAttribute("tracking",delivery.tracking(p.getName(),id));return "account/tracking";}
}
