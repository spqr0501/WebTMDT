package com.example.demo.dto;
import jakarta.validation.constraints.*;
import com.example.demo.entity.PaymentRecord;
import java.math.BigDecimal;
public class PaymentRecordForm {
 @NotNull private PaymentRecord.Kind kind=PaymentRecord.Kind.RECEIPT;
 @NotNull @DecimalMin("1") @DecimalMax("99999999999999") @Digits(integer=14,fraction=0) private BigDecimal amount;
 @NotBlank @Size(max=200) private String reference;
 @NotBlank @Pattern(regexp="[a-fA-F0-9-]{36}") private String token;
 public PaymentRecord.Kind getKind(){return kind;}public void setKind(PaymentRecord.Kind value){kind=value;}
 public BigDecimal getAmount(){return amount;}public void setAmount(BigDecimal value){amount=value;}
 public String getReference(){return reference;}public void setReference(String value){reference=value;}
 public String getToken(){return token;}public void setToken(String value){token=value;}
}
