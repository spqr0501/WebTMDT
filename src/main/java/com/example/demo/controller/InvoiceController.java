package com.example.demo.controller;
import com.example.demo.service.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.http.*;
import java.security.Principal;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletResponse;
@Controller
public class InvoiceController {
 private final InvoiceService invoices;private final OrderService orders;private final CustomCakeService custom;private final CustomerOrderRepository orderRepository;private final CustomCakeRequestRepository customRepository;
 public InvoiceController(InvoiceService invoices,OrderService orders,CustomCakeService custom,CustomerOrderRepository orderRepository,CustomCakeRequestRepository customRepository){this.invoices=invoices;this.orders=orders;this.custom=custom;this.orderRepository=orderRepository;this.customRepository=customRepository;}
 @GetMapping("/orders/{id}/invoice") public String order(@PathVariable Long id,Principal p,Model m,HttpServletResponse response){return view(invoices.order(orders.owned(p.getName(),id)),"/orders/"+id,m,response);}
 @GetMapping("/admin/orders/{id}/invoice") public String adminOrder(@PathVariable Long id,Model m,HttpServletResponse response){return view(invoices.order(orderRepository.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn."))),"/admin/orders/"+id,m,response);}
 @GetMapping("/custom-cakes/{id}/invoice") public String custom(@PathVariable Long id,Principal p,Model m,HttpServletResponse response){return view(invoices.custom(custom.owned(p.getName(),id)),"/custom-cakes/"+id,m,response);}
 @GetMapping("/admin/custom-cakes/{id}/invoice") public String adminCustom(@PathVariable Long id,Model m,HttpServletResponse response){return view(invoices.custom(customRepository.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu."))),"/admin/custom-cakes/"+id,m,response);}
 @GetMapping("/orders/{id}/invoice.pdf") public ResponseEntity<byte[]> orderPdf(@PathVariable Long id,Principal p){return download(invoices.order(orders.owned(p.getName(),id)));}
 @GetMapping("/admin/orders/{id}/invoice.pdf") public ResponseEntity<byte[]> adminOrderPdf(@PathVariable Long id){return download(invoices.order(orderRepository.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn."))));}
 @GetMapping("/custom-cakes/{id}/invoice.pdf") public ResponseEntity<byte[]> customPdf(@PathVariable Long id,Principal p){return download(invoices.custom(custom.owned(p.getName(),id)));}
 @GetMapping("/admin/custom-cakes/{id}/invoice.pdf") public ResponseEntity<byte[]> adminCustomPdf(@PathVariable Long id){return download(invoices.custom(customRepository.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy yêu cầu."))));}
 private String view(InvoiceService.Invoice invoice,String base,Model model,HttpServletResponse response){response.setHeader("Cache-Control","no-store");model.addAttribute("invoice",invoice);model.addAttribute("orderUrl",base);model.addAttribute("pdfUrl",base+"/invoice.pdf");return "invoice/view";}
 private ResponseEntity<byte[]> download(InvoiceService.Invoice invoice){byte[] data=invoices.pdf(invoice);return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).contentLength(data.length).cacheControl(CacheControl.noStore()).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename("Hoa-don-"+invoice.code()+".pdf",StandardCharsets.UTF_8).build().toString()).body(data);}
}
