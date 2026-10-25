package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class OptionForm {
@NotBlank @Size(max=50) private String sizeLabel;
public String getSizeLabel() { return sizeLabel; }
public void setSizeLabel(String v) { sizeLabel=v; }
@NotBlank @Size(max=80) private String flavor;
public String getFlavor() { return flavor; }
public void setFlavor(String v) { flavor=v; }
@NotNull @DecimalMin("0") @DecimalMax("999999999") @Digits(integer=9,fraction=0) private BigDecimal extraPrice = BigDecimal.ZERO;
public BigDecimal getExtraPrice() { return extraPrice; }
public void setExtraPrice(BigDecimal v) { extraPrice=v; }
}
