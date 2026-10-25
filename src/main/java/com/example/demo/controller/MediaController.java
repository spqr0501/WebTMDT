package com.example.demo.controller;
import com.example.demo.service.MediaService;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
public class MediaController {
 private final MediaService media;public MediaController(MediaService media){this.media=media;}
 @GetMapping("/media/{name:.+}") public ResponseEntity<Resource> get(@PathVariable String name){
  var file=media.resolve(name);String type=name.endsWith(".mp4")?"video/mp4":name.endsWith(".png")?"image/png":name.endsWith(".webp")?"image/webp":"image/jpeg";
  return ResponseEntity.ok().header("X-Content-Type-Options","nosniff").contentType(MediaType.parseMediaType(type)).body(new FileSystemResource(file));
 }
}
