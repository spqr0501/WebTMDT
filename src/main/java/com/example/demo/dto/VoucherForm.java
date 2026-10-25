package com.example.demo.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.demo.entity.*;

public class VoucherForm {
 private Long id;
@NotBlank @Pattern(regexp="[a-zA-Z0-9_-]{3,40}") private String code;
@NotNull private DiscountType type = DiscountType.PERCENT;
@NotNull @DecimalMin("1") @DecimalMax("999999999") @Digits(integer=9,fraction=0) private BigDecimal amount;
@NotNull @DecimalMin("0") @DecimalMax("999999999999") @Digits(integer=12,fraction=0) private BigDecimal minOrder = BigDecimal.ZERO;
@DecimalMin("1") @DecimalMax("999999999999") @Digits(integer=12,fraction=0) private BigDecimal maxDiscount;
@NotNull @org.springframework.format.annotation.DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") private LocalDateTime startsAt;
@NotNull @org.springframework.format.annotation.DateTimeFormat(pattern="yyyy-MM-dd'T'HH:mm") private LocalDateTime endsAt;
@Min(1) @Max(1000000) private int usageLimit = 100;
@Min(1) @Max(1000) private int perUserLimit = 1;
 private boolean active = true;
public Long getId() {return id;} public void setId(Long v) {id=v;}
public String getCode() {return code;} public void setCode(String v) {code=v;}
public DiscountType getType() {return type;} public void setType(DiscountType v) {type=v;}
public BigDecimal getAmount() {return amount;} public void setAmount(BigDecimal v) {amount=v;}
public BigDecimal getMinOrder() {return minOrder;} public void setMinOrder(BigDecimal v) {minOrder=v;}
public BigDecimal getMaxDiscount() {return maxDiscount;} public void setMaxDiscount(BigDecimal v) {maxDiscount=v;}
public LocalDateTime getStartsAt() {return startsAt;} public void setStartsAt(LocalDateTime v) {startsAt=v;}
public LocalDateTime getEndsAt() {return endsAt;} public void setEndsAt(LocalDateTime v) {endsAt=v;}
public int getUsageLimit() {return usageLimit;} public void setUsageLimit(int v) {usageLimit=v;}
public int getPerUserLimit() {return perUserLimit;} public void setPerUserLimit(int v) {perUserLimit=v;}
public boolean getActive() {return active;} public void setActive(boolean v) {active=v;}
}
