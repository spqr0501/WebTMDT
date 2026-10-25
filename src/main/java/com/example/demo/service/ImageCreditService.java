package com.example.demo.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
public class ImageCreditService {
 private final List<Credit> credits;
 public ImageCreditService(ObjectMapper mapper) throws IOException {
  try(var input=new ClassPathResource("static/images/banh/SOURCES.json").getInputStream()) {
   credits=List.copyOf(mapper.readValue(input,new TypeReference<List<Credit>>(){}));
  }
 }
 public List<Credit> all(){return credits;}
 public Credit forImage(String url){return credits.stream().filter(c->c.imageUrl().equals(url)).findFirst().orElse(null);}
 @JsonIgnoreProperties(ignoreUnknown=true)
 public record Credit(String productName,String imageUrl,String artist,String license,String licenseUrl,String sourceTitle,String sourcePage,String changes) {}
}
