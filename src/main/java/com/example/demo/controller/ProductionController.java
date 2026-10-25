package com.example.demo.controller;
import com.example.demo.service.ProductionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import java.time.LocalDate;
@Controller
public class ProductionController {
 private final ProductionService production;
 public ProductionController(ProductionService production){this.production=production;}
 @GetMapping("/admin/production") public String page(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(defaultValue="active") String mode,Model m){m.addAttribute("schedule",production.schedule(from,to,mode));return "admin/production";}
}
