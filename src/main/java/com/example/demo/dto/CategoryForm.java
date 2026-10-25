package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.entity.*;

public class CategoryForm {
 private Long id;
@NotBlank @Size(max=100) private String name;
@Size(max=1000) private String description;
public Long getId() {return id;} public void setId(Long v) {id=v;}
public String getName() {return name;} public void setName(String v) {name=v;}
public String getDescription() {return description;} public void setDescription(String v) {description=v;}
}
