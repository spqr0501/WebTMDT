package com.example.demo.controller;
import com.example.demo.config.StripeSettings;
import com.example.demo.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.net.Webhook;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import jakarta.servlet.http.HttpServletRequest;
@RestController
public class StripeWebhookController {
 private final StripeSettings settings;private final StripePaymentService payments;private final ObjectMapper mapper;
 public StripeWebhookController(StripeSettings settings,StripePaymentService payments,ObjectMapper mapper){this.settings=settings;this.payments=payments;this.mapper=mapper;}
 @PostMapping("/payments/stripe/webhook") public ResponseEntity<?> webhook(HttpServletRequest req,@RequestHeader(name="Stripe-Signature",required=false) String signature)throws java.io.IOException{
  if(!settings.webhookReady())return ResponseEntity.status(503).body("Webhook chưa cấu hình");
  if(signature==null||signature.length()>2048)return ResponseEntity.badRequest().body("Chữ ký không hợp lệ");byte[] bytes=req.getInputStream().readNBytes(262145);if(bytes.length>262144)return ResponseEntity.status(413).build();String body=new String(bytes,java.nio.charset.StandardCharsets.UTF_8);
  try{var event=Webhook.constructEvent(body,signature,settings.webhookSecret());if(Boolean.TRUE.equals(event.getLivemode()))return ResponseEntity.badRequest().body("Chỉ nhận sandbox");var node=mapper.readTree(body);if(node.path("livemode").asBoolean(true))return ResponseEntity.badRequest().body("Chỉ nhận sandbox");payments.event(event.getType(),node.path("data").path("object"));return ResponseEntity.ok(java.util.Map.of("received",true));}
  catch(com.stripe.exception.SignatureVerificationException|IllegalArgumentException e){return ResponseEntity.badRequest().body("Webhook không hợp lệ");}
  catch(BusinessException e){return ResponseEntity.badRequest().body("Dữ liệu thanh toán không khớp");}
 }
}
