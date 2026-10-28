package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="giao_dich_stripe")
public class StripePayment {
 public enum Status { CREATING,OPEN,PAID,EXPIRED,REVIEW,RESOLVED }
 @Id @Column(name="id",length=36) private String id;
 @ManyToOne(optional=false) @JoinColumn(name="nguoi_dung_id") private User user;
 @ManyToOne @JoinColumn(name="don_hang_id") private CustomerOrder order;
 @ManyToOne @JoinColumn(name="banh_rieng_id") private CustomCakeRequest customCake;
 @Column(name="so_tien",nullable=false,precision=14,scale=0) private BigDecimal amount;
 @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="trang_thai",nullable=false,length=20) private Status status=Status.CREATING;
 @Column(name="stripe_session_id",length=255) private String sessionId;
 @Column(name="stripe_payment_intent",length=255) private String paymentIntent;
 @Column(name="duong_dan_thanh_toan",length=2000) private String checkoutUrl;
 @Column(name="ngay_tao",nullable=false) private Instant createdAt=Instant.now();
 @Column(name="het_han_luc") private Instant expiresAt;
 @Column(name="xac_nhan_luc") private Instant paidAt;
 @org.hibernate.annotations.Nationalized @Column(name="ghi_chu",length=500) private String note;
 public String getId(){return id;}public void setId(String v){id=v;}
 public User getUser(){return user;}public void setUser(User v){user=v;}
 public CustomerOrder getOrder(){return order;}public void setOrder(CustomerOrder v){order=v;}
 public CustomCakeRequest getCustomCake(){return customCake;}public void setCustomCake(CustomCakeRequest v){customCake=v;}
 public BigDecimal getAmount(){return amount;}public void setAmount(BigDecimal v){amount=v;}
 public Status getStatus(){return status;}public void setStatus(Status v){status=v;}
 public String getSessionId(){return sessionId;}public void setSessionId(String v){sessionId=v;}
 public String getPaymentIntent(){return paymentIntent;}public void setPaymentIntent(String v){paymentIntent=v;}
 public String getCheckoutUrl(){return checkoutUrl;}public void setCheckoutUrl(String v){checkoutUrl=v;}
 public Instant getCreatedAt(){return createdAt;}public void setCreatedAt(Instant v){createdAt=v;}
 public Instant getExpiresAt(){return expiresAt;}public void setExpiresAt(Instant v){expiresAt=v;}
 public Instant getPaidAt(){return paidAt;}public void setPaidAt(Instant v){paidAt=v;}
 public String getNote(){return note;}public void setNote(String v){note=v;}
 public String getOrderUrl(){return order!=null?"/orders/"+order.getId():"/custom-cakes/"+customCake.getId();}
 public String getAdminUrl(){return order!=null?"/admin/orders/"+order.getId():"/admin/custom-cakes/"+customCake.getId();}
 public String getLabel(){return switch(status){case CREATING->"Đang tạo phiên";case OPEN->"Chờ thanh toán";case PAID->"Đã xác nhận thanh toán thử";case EXPIRED->"Phiên đã hết hạn";case REVIEW->"Đã nhận thông báo · cần đối soát";case RESOLVED->"Đã đối soát thủ công";};}
@org.hibernate.annotations.Nationalized @Column(name="ket_qua_doi_soat",length=1000) private String resolution;
@Column(name="nguoi_doi_soat",length=190) private String resolvedBy;
@Column(name="doi_soat_luc") private Instant resolvedAt;
public String getResolution(){return resolution;}public void setResolution(String v){resolution=v;}
public String getResolvedBy(){return resolvedBy;}public void setResolvedBy(String v){resolvedBy=v;}
public Instant getResolvedAt(){return resolvedAt;}public void setResolvedAt(Instant v){resolvedAt=v;}
}
