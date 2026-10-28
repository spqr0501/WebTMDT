package com.example.demo.dto;
import jakarta.validation.constraints.*;
public class ShipperForm {
 private Long id;
 @NotBlank @Size(max=100) private String name;
 @NotBlank @Email @Size(max=190) private String email;
 @NotBlank @Pattern(regexp="[0-9+ ()-]{9,20}") private String phone;
 @NotBlank @Size(max=100) private String vehicle;
 @Size(max=72) private String password;
 private boolean enabled=true;
 public Long getId(){return id;}public void setId(Long v){id=v;}
 public String getName(){return name;}public void setName(String v){name=v;}
 public String getEmail(){return email;}public void setEmail(String v){email=v;}
 public String getPhone(){return phone;}public void setPhone(String v){phone=v;}
 public String getVehicle(){return vehicle;}public void setVehicle(String v){vehicle=v;}
 public String getPassword(){return password;}public void setPassword(String v){password=v;}
 public boolean getEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}
}
