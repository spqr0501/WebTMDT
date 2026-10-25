package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Embeddable
public class PaymentSummary {
 @Column(name="tong_da_hoan",nullable=false,precision=14,scale=0) private BigDecimal refunded=BigDecimal.ZERO;
 public BigDecimal getRefunded(){return refunded;} public void setRefunded(BigDecimal v){refunded=v;}
 @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
 @Column(name="ke_hoach_thanh_toan",nullable=false,length=20) private PaymentPlan plan=PaymentPlan.ON_RECEIPT;
 @Column(name="can_tra_truoc",nullable=false,precision=14,scale=0) private BigDecimal requiredUpfront=BigDecimal.ZERO;
 @Column(name="da_thanh_toan",nullable=false,precision=14,scale=0) private BigDecimal paid=BigDecimal.ZERO;
 public PaymentPlan getPlan(){return plan;} public void setPlan(PaymentPlan value){plan=value;}
 public BigDecimal getRequiredUpfront(){return requiredUpfront;} public void setRequiredUpfront(BigDecimal value){requiredUpfront=value;}
 public BigDecimal getPaid(){return paid;} public void setPaid(BigDecimal value){paid=value;}
 public BigDecimal remaining(BigDecimal total){return total==null?BigDecimal.ZERO:total.subtract(paid).subtract(refunded).max(BigDecimal.ZERO);}
 public BigDecimal getUpfrontRemaining(){return requiredUpfront.subtract(paid).max(BigDecimal.ZERO);}
 public String status(BigDecimal total){if(refunded.signum()>0)return paid.signum()==0?"Đã hoàn toàn bộ khoản đã nhận":"Đã hoàn một phần";if(paid.signum()==0)return "Chưa ghi nhận tiền";if(total!=null&&paid.compareTo(total)>=0)return "Đã trả đủ";return requiredUpfront.signum()>0&&paid.compareTo(requiredUpfront)>=0?"Đã nhận đủ cọc":"Đã nhận một phần";}
}
