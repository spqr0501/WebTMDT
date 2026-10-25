package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.entity.*;

public class AddressForm {
@NotBlank @Size(max=100) private String recipient;
@NotBlank @Pattern(regexp="[0-9+ ()-]{9,20}") private String phone;
@NotBlank @Size(max=500) private String detail;
public String getRecipient() {return recipient;} public void setRecipient(String v) {recipient=v;}
public String getPhone() {return phone;} public void setPhone(String v) {phone=v;}
public String getDetail() {return detail;} public void setDetail(String v) {detail=v;}
}
