package com.example.demo.dto;
import jakarta.validation.constraints.*;
public class PasswordChangeForm {
 @NotBlank(message="Nhập mật khẩu hiện tại.") @Size(max=72) private String currentPassword;
 @NotBlank(message="Nhập mật khẩu mới.") @Size(min=8,max=72,message="Mật khẩu mới từ 8 đến 72 ký tự.") private String newPassword;
 @NotBlank(message="Nhập lại mật khẩu mới.") @Size(max=72) private String confirmation;
 public String getCurrentPassword(){return currentPassword;}public void setCurrentPassword(String v){currentPassword=v;}
 public String getNewPassword(){return newPassword;}public void setNewPassword(String v){newPassword=v;}
 public String getConfirmation(){return confirmation;}public void setConfirmation(String v){confirmation=v;}
}
