package com.example.demo.service;
import com.example.demo.entity.*;
import java.math.*;
public final class PaymentPolicy {
 private PaymentPolicy(){}
 public static void configure(PaymentSummary summary,BigDecimal total,FulfillmentMethod receive,PaymentMethod method,PaymentPlan selected){
  if(receive==FulfillmentMethod.PICKUP){
   if(selected!=PaymentPlan.DEPOSIT&&selected!=PaymentPlan.FULL)throw new BusinessException("Nhận tại cửa hàng cần chọn cọc 50% hoặc trả đủ trước.");
   summary.setPlan(selected);summary.setRequiredUpfront(selected==PaymentPlan.FULL?total:total.multiply(new BigDecimal("0.5")).setScale(0,RoundingMode.CEILING));
  }else{summary.setPlan(method==PaymentMethod.BANK_TRANSFER?PaymentPlan.FULL:PaymentPlan.ON_RECEIPT);summary.setRequiredUpfront(method==PaymentMethod.BANK_TRANSFER?total:BigDecimal.ZERO);}
 }
 public static void requireUpfront(PaymentSummary summary){if(summary.getUpfrontRemaining().signum()>0)throw new BusinessException("Chưa đủ tiền cần thanh toán trước. Hãy đối soát và ghi nhận tiền trước khi xác nhận / chuẩn bị bánh.");}
 public static void requireSettled(PaymentSummary summary,BigDecimal total){if(summary.remaining(total).signum()>0)throw new BusinessException("Cần ghi nhận đủ số tiền còn lại trước khi hoàn thành đơn nhận tại cửa hàng.");}
}
