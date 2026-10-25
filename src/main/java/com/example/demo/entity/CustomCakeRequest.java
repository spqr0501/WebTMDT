package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="yeu_cau_banh_rieng")
public class CustomCakeRequest {
@Column(name="ngay_hoan_thanh") private LocalDateTime completedAt;
public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime v){completedAt=v;}
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
public Long getId(){return id;} public void setId(Long value){this.id=value;}
@ManyToOne(optional=false) @JoinColumn(name="nguoi_dung_id") private User user;
public User getUser(){return user;} public void setUser(User value){this.user=value;}
@org.hibernate.annotations.Nationalized @Column(name="nguoi_nhan",nullable=false,length=100) private String recipient;
public String getRecipient(){return recipient;} public void setRecipient(String value){this.recipient=value;}
@Column(name="so_dien_thoai",nullable=false,length=20) private String phone;
public String getPhone(){return phone;} public void setPhone(String value){this.phone=value;}
@org.hibernate.annotations.Nationalized @Column(name="yeu_cau",nullable=false,length=3000) private String description;
public String getDescription(){return description;} public void setDescription(String value){this.description=value;}
@org.hibernate.annotations.Nationalized @Column(name="huong_vi",nullable=false,length=100) private String flavor;
public String getFlavor(){return flavor;} public void setFlavor(String value){this.flavor=value;}
@Column(name="so_nguoi",nullable=false) private int servings = 1;
public int getServings(){return servings;} public void setServings(int value){this.servings=value;}
@Column(name="ngan_sach",nullable=false,precision=14,scale=0) private BigDecimal budget;
public BigDecimal getBudget(){return budget;} public void setBudget(BigDecimal value){this.budget=value;}
@Column(name="ngay_nhan",nullable=false) private LocalDate receiveDate;
public LocalDate getReceiveDate(){return receiveDate;} public void setReceiveDate(LocalDate value){this.receiveDate=value;}
@Column(name="khung_gio",nullable=false,length=20) private String receiveSlot;
public String getReceiveSlot(){return receiveSlot;} public void setReceiveSlot(String value){this.receiveSlot=value;}
@Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="hinh_thuc_nhan",nullable=false,length=30) private FulfillmentMethod fulfillmentMethod = FulfillmentMethod.DELIVERY;
public FulfillmentMethod getFulfillmentMethod(){return fulfillmentMethod;} public void setFulfillmentMethod(FulfillmentMethod value){this.fulfillmentMethod=value;}
@org.hibernate.annotations.Nationalized @Column(name="dia_chi",nullable=false,length=500) private String address;
public String getAddress(){return address;} public void setAddress(String value){this.address=value;}
@Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="phuong_thuc_thanh_toan",nullable=false,length=30) private PaymentMethod paymentMethod = PaymentMethod.COD;
public PaymentMethod getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(PaymentMethod value){this.paymentMethod=value;}
@Column(name="anh_tham_khao",length=500) private String referenceImage;
public String getReferenceImage(){return referenceImage;} public void setReferenceImage(String value){this.referenceImage=value;}
@org.hibernate.annotations.Nationalized @Column(name="loi_chuc",length=100) private String cakeMessage;
public String getCakeMessage(){return cakeMessage;} public void setCakeMessage(String value){this.cakeMessage=value;}
@Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="trang_thai",nullable=false,length=30) private CustomCakeStatus status = CustomCakeStatus.SENT;
public CustomCakeStatus getStatus(){return status;} public void setStatus(CustomCakeStatus value){this.status=value;}
@Column(name="tong_bao_gia",precision=14,scale=0) private BigDecimal quotedTotal;
public BigDecimal getQuotedTotal(){return quotedTotal;} public void setQuotedTotal(BigDecimal value){this.quotedTotal=value;}
@org.hibernate.annotations.Nationalized @Column(name="phan_hoi",length=1500) private String adminReply;
public String getAdminReply(){return adminReply;} public void setAdminReply(String value){this.adminReply=value;}
@Column(name="ma_chong_dat_trung",nullable=false,length=36,unique=true) private String token;
public String getToken(){return token;} public void setToken(String value){this.token=value;}
@Column(name="ngay_tao",nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime value){this.createdAt=value;}
@Column(name="ngay_cap_nhat",nullable=false) private LocalDateTime updatedAt = LocalDateTime.now();
public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime value){this.updatedAt=value;}
@Version @Column(name="phien_ban",nullable=false) private long version = 0;
public long getVersion(){return version;} public void setVersion(long value){this.version=value;}


@Embedded private PaymentSummary payment=new PaymentSummary();
public PaymentSummary getPayment(){return payment;}public void setPayment(PaymentSummary value){payment=value;}
}
