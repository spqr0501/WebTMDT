package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.entity.*;

public class AddressForm {
private boolean defaultAddress;
public boolean getDefaultAddress(){return defaultAddress;} public void setDefaultAddress(boolean v){defaultAddress=v;}

@NotBlank @Size(max=100) private String recipient;
@NotBlank @Pattern(regexp="0[35789][0-9]{8}",message="Số di động Việt Nam phải có 10 số, đầu 03/05/07/08/09.") private String phone;
@NotBlank @Size(max=500) private String detail;
public String getRecipient() {return recipient;} public void setRecipient(String v) {recipient=v;}
public String getPhone() {return phone;} public void setPhone(String v) {phone=com.example.demo.service.VietnamPhone.normalize(v);}
public String getDetail() {return detail;} public void setDetail(String v) {detail=v;}

@DecimalMin("-90") @DecimalMax("90") private Double latitude;
@DecimalMin("-180") @DecimalMax("180") private Double longitude;
public Double getLatitude(){return latitude;}public void setLatitude(Double v){latitude=v;}
public Double getLongitude(){return longitude;}public void setLongitude(Double v){longitude=v;}

}
