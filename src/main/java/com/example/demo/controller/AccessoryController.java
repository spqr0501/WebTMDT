package com.example.demo.controller;
import com.example.demo.dto.AccessoryForm;
import com.example.demo.service.*;
import com.example.demo.repository.AccessoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.beans.BeanUtils;
import jakarta.validation.Valid;
@Controller @RequestMapping("/admin/accessories")
public class AccessoryController {
 private final AccessoryRepository accessories;private final AccessoryService service;
 public AccessoryController(AccessoryRepository accessories,AccessoryService service){this.accessories=accessories;this.service=service;}
 private void model(Model m){m.addAttribute("accessories",accessories.findAll(org.springframework.data.domain.Sort.by("id")));}
 @GetMapping public String page(Model m){model(m);m.addAttribute("form",new AccessoryForm());return "admin/accessories";}
 @GetMapping("/{id}/edit") public String edit(@PathVariable Long id,Model m){model(m);AccessoryForm form=new AccessoryForm();BeanUtils.copyProperties(accessories.findById(id).orElseThrow(),form);m.addAttribute("form",form);return "admin/accessories";}
 @PostMapping("/save") public String save(@Valid @ModelAttribute("form") AccessoryForm f,BindingResult errors,Model m){if(errors.hasErrors()){model(m);return "admin/accessories";}try{service.save(f);}catch(BusinessException e){errors.reject("accessory",e.getMessage());model(m);return "admin/accessories";}return "redirect:/admin/accessories";}
 @PostMapping("/{id}/stock") public String stock(@PathVariable Long id,@RequestParam int delta){service.stock(id,delta);return "redirect:/admin/accessories";}
 @PostMapping("/{id}/delete") public String delete(@PathVariable Long id,org.springframework.web.servlet.mvc.support.RedirectAttributes flash){boolean deleted=service.delete(id);flash.addFlashAttribute("success",deleted?"Đã xóa phụ kiện chưa sử dụng.":"Phụ kiện đã có trong đơn hàng: đã chuyển sang ngừng bán để giữ lịch sử.");return "redirect:/admin/accessories";}
}
