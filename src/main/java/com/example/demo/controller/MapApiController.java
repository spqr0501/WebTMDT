package com.example.demo.controller;
import com.example.demo.service.GeoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
@RestController @RequestMapping("/api/maps")
public class MapApiController {
 private final GeoService geo;public MapApiController(GeoService geo){this.geo=geo;}
 @GetMapping("/search") public ResponseEntity<?> search(@RequestParam String q){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(geo.search(q));}
 @GetMapping("/reverse") public ResponseEntity<?> reverse(@RequestParam double lat,@RequestParam double lng){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(geo.reverse(lat,lng));}
}
