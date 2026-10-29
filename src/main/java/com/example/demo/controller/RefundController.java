package com.example.demo.controller;
import com.example.demo.repository.*;
import com.example.demo.entity.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
@Controller
public class RefundController {
 private final CustomerOrderRepository orders;private final CustomCakeRequestRepository custom;
 public RefundController(CustomerOrderRepository orders,CustomCakeRequestRepository custom){this.orders=orders;this.custom=custom;}
 @GetMapping("/admin/refunds") public String pending(@RequestParam(defaultValue="0") int orderPage,@RequestParam(defaultValue="0") int customPage,Model model){
  var statuses=java.util.List.of(CustomCakeStatus.CANCELLED,CustomCakeStatus.REJECTED);
  model.addAttribute("orders",orders.findByStatusAndPaymentPaidGreaterThan(OrderStatus.CANCELLED,BigDecimal.ZERO,PageRequest.of(Math.max(0,orderPage),15,Sort.by("id").descending())));
  model.addAttribute("customRequests",custom.findByStatusInAndPaymentPaidGreaterThan(statuses,BigDecimal.ZERO,PageRequest.of(Math.max(0,customPage),15,Sort.by("id").descending())));
  return "admin/refunds";
 }
}
