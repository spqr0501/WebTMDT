package com.example.demo.dto;
import jakarta.validation.constraints.*;
public class ProfileForm {
 @NotBlank(message="Vui lòng nhập họ tên.") @Size(max=100,message="Họ tên tối đa 100 ký tự.") private String name;
 @Pattern(regexp="^$|[0-9+ ()-]{9,20}",message="Số điện thoại cần từ 9 đến 20 ký tự hợp lệ.") private String phone;
 public String getName(){return name;}public void setName(String v){name=v;}
 public String getPhone(){return phone;}public void setPhone(String v){phone=v;}
}
