package com.example.demo.controller;
import com.example.demo.service.DeliveryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.time.Instant;
import java.security.Principal;
@RestController
public class DeliveryApiController {
 private final DeliveryService delivery;public DeliveryApiController(DeliveryService delivery){this.delivery=delivery;}
 public record Position(String trackingKey,Double latitude,Double longitude,Double accuracy,Instant capturedAt){}
 @GetMapping("/api/delivery/{id}") public ResponseEntity<?> track(@PathVariable Long id,Principal p){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(delivery.tracking(p.getName(),id));}
 @PostMapping("/shipper/api/orders/{id}/begin") public ResponseEntity<?> begin(@PathVariable Long id,Principal p){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(java.util.Map.of("key",delivery.begin(p.getName(),id)));}
 @PostMapping("/shipper/api/orders/{id}/location") public ResponseEntity<?> location(@PathVariable Long id,@RequestBody Position point,Principal p){delivery.location(p.getName(),id,point.trackingKey(),point.latitude(),point.longitude(),point.accuracy(),point.capturedAt());return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(java.util.Map.of("ok",true));}
 @PostMapping("/shipper/api/orders/{id}/stop") public ResponseEntity<?> stop(@PathVariable Long id,Principal p){delivery.stop(p.getName(),id);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(java.util.Map.of("ok",true));}
}
