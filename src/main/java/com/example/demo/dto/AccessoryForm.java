package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import com.example.demo.entity.*;
public class AccessoryForm {
 private Long id;
public Long getId(){return id;} public void setId(Long value){this.id=value;}
@NotBlank @Size(max=100) private String name;
public String getName(){return name;} public void setName(String value){this.name=value;}
@NotNull @DecimalMin("0") @DecimalMax("999999999") @Digits(integer=9,fraction=0) private BigDecimal price = BigDecimal.ZERO;
public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal value){this.price=value;}
@Min(0) @Max(1000000) private int stock = 0;
public int getStock(){return stock;} public void setStock(int value){this.stock=value;}
 private boolean active = true;
public boolean getActive(){return active;} public void setActive(boolean value){this.active=value;}
}
