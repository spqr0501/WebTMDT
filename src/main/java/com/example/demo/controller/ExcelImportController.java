package com.example.demo.controller;
import com.example.demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import org.springframework.http.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
@Controller @RequestMapping("/admin/import")
public class ExcelImportController {
 private final ExcelImportService service;
 public ExcelImportController(ExcelImportService service){this.service=service;}
 private void model(Model m){m.addAttribute("kinds",ExcelImportService.Kind.values());}
 @GetMapping public String page(Model m){model(m);return "admin/import";}
 @GetMapping("/template.xlsx") public ResponseEntity<byte[]> template(@RequestParam ExcelImportService.Kind kind){return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"Mau-"+kind+".xlsx\"").cacheControl(CacheControl.noStore()).body(service.template(kind));}
 @PostMapping("/preview") public String preview(@RequestParam ExcelImportService.Kind kind,@RequestParam MultipartFile file,Principal p,HttpSession session,Model m){synchronized(session){session.removeAttribute("excelPreview");try{var preview=service.preview(file,kind,p.getName());session.setAttribute("excelPreview",preview);m.addAttribute("preview",preview);}catch(BusinessException e){m.addAttribute("formError",e.getMessage());}}model(m);return "admin/import";}
 @PostMapping("/commit") public String commit(@RequestParam String token,Principal p,HttpSession session,RedirectAttributes flash){synchronized(session){Object stored=session.getAttribute("excelPreview");if(!(stored instanceof ExcelImportService.Preview preview)||!preview.token().equals(token))throw new BusinessException("Phiên nhập đã kết thúc. Kiểm tra danh sách trước khi nhập lại.");try{int count=service.commit(preview,p.getName());session.removeAttribute("excelPreview");flash.addFlashAttribute("success","Đã nhập "+count+" dòng Excel thành công.");}catch(BusinessException e){session.removeAttribute("excelPreview");flash.addFlashAttribute("formError",e.getMessage());}}return "redirect:/admin/import";}
}
