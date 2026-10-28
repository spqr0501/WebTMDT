package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.entity.*;

public class CheckoutForm {
private Long addressId;
@NotNull private PaymentMethod paymentMethod = PaymentMethod.COD;
@Size(max=40) private String voucherCode;
@Size(max=500) private String note;
@NotBlank @Size(max=36) private String token;
public Long getAddressId() {return addressId;} public void setAddressId(Long v) {addressId=v;}
public PaymentMethod getPaymentMethod() {return paymentMethod;} public void setPaymentMethod(PaymentMethod v) {paymentMethod=v;}
public String getVoucherCode() {return voucherCode;} public void setVoucherCode(String v) {voucherCode=v;}
public String getNote() {return note;} public void setNote(String v) {note=v;}
public String getToken() {return token;} public void setToken(String v) {token=v;}
@NotNull @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) private java.time.LocalDate deliveryDate;
public java.time.LocalDate getDeliveryDate() { return deliveryDate; }
public void setDeliveryDate(java.time.LocalDate v) { deliveryDate=v; }
@NotBlank @Pattern(regexp="09:00-12:00|13:00-17:00|18:00-20:00",message="Khung giờ giao bánh không hợp lệ.") private String deliverySlot;
public String getDeliverySlot() { return deliverySlot; }
public void setDeliverySlot(String v) { deliverySlot=v; }
@Size(max=100) private String cakeMessage;
public String getCakeMessage() { return cakeMessage; }
public void setCakeMessage(String v) { cakeMessage=v; }
@NotNull private FulfillmentMethod fulfillmentMethod = FulfillmentMethod.DELIVERY;
public FulfillmentMethod getFulfillmentMethod(){return fulfillmentMethod;} public void setFulfillmentMethod(FulfillmentMethod value){this.fulfillmentMethod=value;}
 private boolean preorder = false;
public boolean getPreorder(){return preorder;} public void setPreorder(boolean value){this.preorder=value;}
@Size(max=100) private String pickupRecipient;
public String getPickupRecipient(){return pickupRecipient;} public void setPickupRecipient(String value){this.pickupRecipient=value;}
@Size(max=20) private String pickupPhone;
public String getPickupPhone(){return pickupPhone;} public void setPickupPhone(String value){this.pickupPhone=value;}
@Size(max=10) private java.util.Map<Long,Integer> accessories = new java.util.TreeMap<>();
public java.util.Map<Long,Integer> getAccessories(){return accessories;} public void setAccessories(java.util.Map<Long,Integer> value){this.accessories=value;}


@NotNull private PaymentPlan paymentPlan=PaymentPlan.DEPOSIT;
public PaymentPlan getPaymentPlan(){return paymentPlan;}public void setPaymentPlan(PaymentPlan value){paymentPlan=value;}

private boolean useNewAddress;
private AddressForm newAddress=new AddressForm();
public boolean getUseNewAddress(){return useNewAddress;}public void setUseNewAddress(boolean v){useNewAddress=v;}
public AddressForm getNewAddress(){return newAddress;}public void setNewAddress(AddressForm v){newAddress=v;}

}
