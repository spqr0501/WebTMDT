package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="ma_giam_gia")
public class Voucher {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@Column(name="ma",nullable=false,unique=true,length=40) private String code;
@Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="loai",nullable=false,length=20) private DiscountType type = DiscountType.PERCENT;
@Column(name="gia_tri_giam",nullable=false,precision=14,scale=0) private BigDecimal amount;
@Column(name="gia_tri_don_toi_thieu",nullable=false,precision=14,scale=0) private BigDecimal minOrder = BigDecimal.ZERO;
@Column(name="giam_toi_da",precision=14,scale=0) private BigDecimal maxDiscount;
@Column(name="bat_dau_luc",nullable=false) private LocalDateTime startsAt;
@Column(name="ket_thuc_luc",nullable=false) private LocalDateTime endsAt;
@Column(name="gioi_han_luot_dung",nullable=false) private int usageLimit = 100;
@Column(name="gioi_han_moi_nguoi",nullable=false) private int perUserLimit = 1;
@Column(name="so_luot_da_dung",nullable=false) private int usedCount = 0;
@Column(name="dang_ap_dung",nullable=false) private boolean active = true;
public Long getId() { return id; }
public void setId(Long value) { this.id=value; }
public String getCode() { return code; }
public void setCode(String value) { this.code=value; }
public DiscountType getType() { return type; }
public void setType(DiscountType value) { this.type=value; }
public BigDecimal getAmount() { return amount; }
public void setAmount(BigDecimal value) { this.amount=value; }
public BigDecimal getMinOrder() { return minOrder; }
public void setMinOrder(BigDecimal value) { this.minOrder=value; }
public BigDecimal getMaxDiscount() { return maxDiscount; }
public void setMaxDiscount(BigDecimal value) { this.maxDiscount=value; }
public LocalDateTime getStartsAt() { return startsAt; }
public void setStartsAt(LocalDateTime value) { this.startsAt=value; }
public LocalDateTime getEndsAt() { return endsAt; }
public void setEndsAt(LocalDateTime value) { this.endsAt=value; }
public int getUsageLimit() { return usageLimit; }
public void setUsageLimit(int value) { this.usageLimit=value; }
public int getPerUserLimit() { return perUserLimit; }
public void setPerUserLimit(int value) { this.perUserLimit=value; }
public int getUsedCount() { return usedCount; }
public void setUsedCount(int value) { this.usedCount=value; }
public boolean getActive() { return active; }
public void setActive(boolean value) { this.active=value; }
@Transient public String getStatusLabel() {
 LocalDateTime now = LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
 if (endsAt != null && !now.isBefore(endsAt)) return "Đã hết hạn";
 if (!active) return "Đã tắt";
 if (startsAt != null && now.isBefore(startsAt)) return "Chưa đến hạn";
 if (usedCount >= usageLimit) return "Hết lượt dùng";
 return "Đang áp dụng";
}
@Transient public boolean getCurrentlyApplicable() { return "Đang áp dụng".equals(getStatusLabel()); }

}
