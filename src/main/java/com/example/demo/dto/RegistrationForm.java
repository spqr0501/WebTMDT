package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.entity.*;

public class RegistrationForm {
@NotBlank @Size(max=100) private String name;
@NotBlank @Email @Size(max=190) private String email;
@NotBlank @Size(min=8,max=72) private String password;
@NotBlank(message="Vui lòng nhập lại mật khẩu.") @Size(min=8,max=72) private String confirmPassword;
public String getName() {return name;} public void setName(String v) {name=v;}
public String getEmail() {return email;} public void setEmail(String v) {email=v;}
public String getPassword() {return password;} public void setPassword(String v) {password=v;}
public String getConfirmPassword() {return confirmPassword;} public void setConfirmPassword(String v) {confirmPassword=v;}
}
