package com.example.demo.dto;
import jakarta.validation.constraints.*;
public class ProfileForm {
 @NotBlank(message="Vui lòng nhập họ tên.") @Size(max=100,message="Họ tên tối đa 100 ký tự.") private String name;
 @Pattern(regexp="^$|0[35789][0-9]{8}",message="Số di động Việt Nam phải có 10 số, đầu 03/05/07/08/09.") private String phone;
 public String getName(){return name;}public void setName(String v){name=v;}
 public String getPhone(){return phone;}public void setPhone(String v){phone=com.example.demo.service.VietnamPhone.normalize(v);}
}
