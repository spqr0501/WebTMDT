package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import java.math.*;
import java.time.LocalDateTime;
import java.util.Locale;
@Service
public class VoucherService {
 private final VoucherRepository vouchers;private final VoucherUsageRepository usages;
 public VoucherService(VoucherRepository vouchers,VoucherUsageRepository usages){this.vouchers=vouchers;this.usages=usages;}
 public String normalize(String code){return code==null?"":code.strip().toUpperCase(Locale.ROOT);}
 public BigDecimal preview(String code,Long userId,BigDecimal subtotal){
  if(normalize(code).isEmpty())return BigDecimal.ZERO;
  return discount(vouchers.findByCode(normalize(code)).orElseThrow(()->new BusinessException("Mã giảm giá không tồn tại.")),userId,subtotal);
 }
 public BigDecimal discount(Voucher v,Long userId,BigDecimal subtotal){
  LocalDateTime now=LocalDateTime.now();
  if(!v.getActive())throw new BusinessException("Mã giảm giá đã ngừng áp dụng.");
  if(now.isBefore(v.getStartsAt())||!now.isBefore(v.getEndsAt()))throw new BusinessException("Mã chưa đến thời gian áp dụng hoặc đã hết hạn.");
  if(subtotal.compareTo(v.getMinOrder())<0)throw new BusinessException("Đơn hàng chưa đạt giá trị tối thiểu "+v.getMinOrder()+" đ.");
  if(v.getUsedCount()>=v.getUsageLimit())throw new BusinessException("Mã đã hết lượt sử dụng.");
  if(usages.countByVoucherIdAndUserId(v.getId(),userId)>=v.getPerUserLimit())throw new BusinessException("Bạn đã dùng hết lượt của mã này.");
  BigDecimal discount=v.getType()==DiscountType.PERCENT?subtotal.multiply(v.getAmount()).divide(BigDecimal.valueOf(100),0,RoundingMode.DOWN):v.getAmount();
  if(v.getMaxDiscount()!=null)discount=discount.min(v.getMaxDiscount());
  return discount.min(subtotal).max(BigDecimal.ZERO);
 }
}
