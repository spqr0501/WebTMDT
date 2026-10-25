package com.example.demo.controller;
import com.example.demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import org.springframework.http.*;
import java.time.LocalDate;
@Controller @RequestMapping("/admin/reports")
public class SalesReportController {
 private final SalesReportService service;private final SalesReportPdfService pdf;
 public SalesReportController(SalesReportService service,SalesReportPdfService pdf){this.service=service;this.pdf=pdf;}
 @GetMapping public String page(@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(defaultValue="COMPLETED") String basis,@RequestParam(defaultValue="ALL") String type,Model m){m.addAttribute("report",service.report(from,to,basis,type));return "admin/reports";}
 @GetMapping("/export.pdf") public ResponseEntity<byte[]> download(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,@RequestParam(defaultValue="COMPLETED") String basis,@RequestParam(defaultValue="ALL") String type){byte[] bytes=pdf.pdf(service.report(from,to,basis,type));return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).cacheControl(CacheControl.noStore()).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"Bao-cao-"+from+"-"+to+".pdf\"").body(bytes);}
}
