package com.example.demo.controller;

import com.example.demo.service.ImageCreditService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ImageCreditController {
 private final ImageCreditService images;
 public ImageCreditController(ImageCreditService images){this.images=images;}
 @GetMapping("/image-credits") public String credits(Model model){model.addAttribute("credits",images.all());return "shop/image-credits";}
}
