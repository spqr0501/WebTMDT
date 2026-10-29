package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import com.example.demo.entity.*;
public class CustomCakeForm {
private Double latitude,longitude;
public Double getLatitude(){return latitude;} public void setLatitude(Double v){latitude=v;}
public Double getLongitude(){return longitude;} public void setLongitude(Double v){longitude=v;}

@NotBlank @Size(max=100) private String recipient;
public String getRecipient(){return recipient;} public void setRecipient(String value){this.recipient=value;}
@NotBlank @Pattern(regexp="0[35789][0-9]{8}",message="Số điện thoại di động Việt Nam phải có 10 số và đầu số hợp lệ (ví dụ 0902592231).") private String phone;
public String getPhone(){return phone;} public void setPhone(String value){this.phone=com.example.demo.service.VietnamPhone.normalize(value);}
@NotBlank @Size(min=20,max=3000) private String description;
public String getDescription(){return description;} public void setDescription(String value){this.description=value;}
@NotBlank @Size(max=100) private String flavor;
public String getFlavor(){return flavor;} public void setFlavor(String value){this.flavor=value;}
@Min(1) @Max(300) private int servings = 8;
public int getServings(){return servings;} public void setServings(int value){this.servings=value;}
@NotNull @DecimalMin("10000") @DecimalMax("999999999") @Digits(integer=9,fraction=0) private BigDecimal budget;
public BigDecimal getBudget(){return budget;} public void setBudget(BigDecimal value){this.budget=value;}
@NotNull @org.springframework.format.annotation.DateTimeFormat(pattern="dd/MM/yyyy") private LocalDate receiveDate;
public LocalDate getReceiveDate(){return receiveDate;} public void setReceiveDate(LocalDate value){this.receiveDate=value;}
@NotBlank @Pattern(regexp="09:00-12:00|13:00-17:00|18:00-20:00") private String receiveSlot = "09:00-12:00";
public String getReceiveSlot(){return receiveSlot;} public void setReceiveSlot(String value){this.receiveSlot=value;}
@NotNull private FulfillmentMethod fulfillmentMethod = FulfillmentMethod.DELIVERY;
public FulfillmentMethod getFulfillmentMethod(){return fulfillmentMethod;} public void setFulfillmentMethod(FulfillmentMethod value){this.fulfillmentMethod=value;}
@Size(max=500) private String address;
public String getAddress(){return address;} public void setAddress(String value){this.address=value;}
@NotNull private PaymentMethod paymentMethod = PaymentMethod.COD;
public PaymentMethod getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(PaymentMethod value){this.paymentMethod=value;}
@Size(max=100) private String cakeMessage;
public String getCakeMessage(){return cakeMessage;} public void setCakeMessage(String value){this.cakeMessage=value;}
@NotBlank @Size(max=36) private String token;
public String getToken(){return token;} public void setToken(String value){this.token=value;}

@NotNull private PaymentPlan paymentPlan=PaymentPlan.DEPOSIT;
public PaymentPlan getPaymentPlan(){return paymentPlan;}public void setPaymentPlan(PaymentPlan value){paymentPlan=value;}
}
