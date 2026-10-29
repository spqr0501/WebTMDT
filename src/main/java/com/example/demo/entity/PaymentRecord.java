package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="lich_su_thanh_toan")
public class PaymentRecord {
 @Column(name="kenh_hoan",length=30) private String refundChannel;
 @org.hibernate.annotations.Nationalized @Column(name="noi_nhan_hoan",length=200) private String refundDestination;
 @org.hibernate.annotations.Nationalized @Column(name="ma_giao_dich_hoan",length=100) private String refundTransactionId;
 public String getRefundChannel(){return refundChannel;}public void setRefundChannel(String v){refundChannel=v;}
 public String getRefundDestination(){return refundDestination;}public void setRefundDestination(String v){refundDestination=v;}
 public String getRefundTransactionId(){return refundTransactionId;}public void setRefundTransactionId(String v){refundTransactionId=v;}

 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="doi_tra_id") private AfterSalesRequest afterSales;
 public AfterSalesRequest getAfterSales(){return afterSales;} public void setAfterSales(AfterSalesRequest v){afterSales=v;}
 public enum Kind { RECEIPT, REFUND }
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="don_hang_id") private CustomerOrder order;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="banh_rieng_id") private CustomCakeRequest customCake;
 @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="loai",nullable=false,length=20) private Kind kind;
 @Column(name="so_tien",nullable=false,precision=14,scale=0) private BigDecimal amount;
 @org.hibernate.annotations.Nationalized @Column(name="tham_chieu",nullable=false,length=200) private String reference;
 @Column(name="nguoi_xac_nhan",nullable=false,length=190) private String actor;
 @Column(name="ma_chong_lap",nullable=false,unique=true,length=36) private String token;
 @Column(name="ngay_tao",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;}
 public CustomerOrder getOrder(){return order;}public void setOrder(CustomerOrder value){order=value;}
 public CustomCakeRequest getCustomCake(){return customCake;}public void setCustomCake(CustomCakeRequest value){customCake=value;}
 public Kind getKind(){return kind;}public void setKind(Kind value){kind=value;}
 public BigDecimal getAmount(){return amount;}public void setAmount(BigDecimal value){amount=value;}
 public String getReference(){return reference;}public void setReference(String value){reference=value;}
 public String getActor(){return actor;}public void setActor(String value){actor=value;}
 public String getToken(){return token;}public void setToken(String value){token=value;}
 public LocalDateTime getCreatedAt(){return createdAt;}
}
