package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.entity.*;

public class ProductForm {
 private Long id;
@NotBlank @Size(max=150) private String name;
@NotBlank @Size(max=5000) private String description;
@NotNull @DecimalMin("1000") @DecimalMax("999999999") @Digits(integer=9,fraction=0) private BigDecimal price;
@NotNull @Min(0) @Max(1000000) private Integer stock = 0;
@NotNull private Long categoryId;
 private boolean active = true;
 private boolean featured = false;
public Long getId() {return id;} public void setId(Long v) {id=v;}
public String getName() {return name;} public void setName(String v) {name=v;}
public String getDescription() {return description;} public void setDescription(String v) {description=v;}
public BigDecimal getPrice() {return price;} public void setPrice(BigDecimal v) {price=v;}
public Integer getStock() {return stock;} public void setStock(Integer v) {stock=v;}
public Long getCategoryId() {return categoryId;} public void setCategoryId(Long v) {categoryId=v;}
public boolean getActive() {return active;} public void setActive(boolean v) {active=v;}
public boolean getFeatured() {return featured;} public void setFeatured(boolean v) {featured=v;}
@Size(max=1000) private String ingredients;
public String getIngredients() { return ingredients; }
public void setIngredients(String v) { ingredients=v; }
@Size(max=500) private String allergens;
public String getAllergens() { return allergens; }
public void setAllergens(String v) { allergens=v; }
@Size(max=1000) private String storageInstructions;
public String getStorageInstructions() { return storageInstructions; }
public void setStorageInstructions(String v) { storageInstructions=v; }
}
