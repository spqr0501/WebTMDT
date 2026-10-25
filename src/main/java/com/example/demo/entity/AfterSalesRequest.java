package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity @Table(name="yeu_cau_doi_tra")
public class AfterSalesRequest {
 public enum Status { REQUESTED, APPROVED, REJECTED, COMPLETED }
 public enum Resolution { REFUND, REPLACEMENT }
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="nguoi_dung_id") private User user;
 @ManyToOne @JoinColumn(name="don_hang_id") private CustomerOrder order;
 @ManyToOne @JoinColumn(name="banh_rieng_id") private CustomCakeRequest customCake;
 @org.hibernate.annotations.Nationalized @Column(name="ly_do",nullable=false,length=2000) private String reason;
 @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="phuong_an",nullable=false,length=30) private Resolution resolution;
 @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="trang_thai",nullable=false,length=30) private Status status=Status.REQUESTED;
 @Column(name="tien_duyet_hoan",nullable=false,precision=14,scale=0) private BigDecimal approvedAmount=BigDecimal.ZERO;
 @Column(name="da_hoan",nullable=false,precision=14,scale=0) private BigDecimal refunded=BigDecimal.ZERO;
 @org.hibernate.annotations.Nationalized @Column(name="phan_hoi",length=2000) private String reply;
 @Column(name="nguoi_xu_ly",length=190) private String actor;
 @Column(name="ngay_gui",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 @Column(name="ngay_xu_ly") private LocalDateTime handledAt;
 @Column(name="ngay_hoan_tat") private LocalDateTime completedAt;
 @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="anh_doi_tra",joinColumns=@JoinColumn(name="yeu_cau_id")) @OrderColumn(name="thu_tu") @Column(name="duong_dan",nullable=false,length=500) private List<String> photos=new ArrayList<>();
 public Long getId(){return id;}
 public User getUser(){return user;} public void setUser(User v){user=v;}
 public CustomerOrder getOrder(){return order;} public void setOrder(CustomerOrder v){order=v;}
 public CustomCakeRequest getCustomCake(){return customCake;} public void setCustomCake(CustomCakeRequest v){customCake=v;}
 public String getReason(){return reason;} public void setReason(String v){reason=v;}
 public Resolution getResolution(){return resolution;} public void setResolution(Resolution v){resolution=v;}
 public Status getStatus(){return status;} public void setStatus(Status v){status=v;}
 public BigDecimal getApprovedAmount(){return approvedAmount;} public void setApprovedAmount(BigDecimal v){approvedAmount=v;}
 public BigDecimal getRefunded(){return refunded;} public void setRefunded(BigDecimal v){refunded=v;}
 public BigDecimal getRefundRemaining(){return approvedAmount.subtract(refunded).max(BigDecimal.ZERO);}
 public String getReply(){return reply;} public void setReply(String v){reply=v;}
 public String getActor(){return actor;} public void setActor(String v){actor=v;}
 public LocalDateTime getCreatedAt(){return createdAt;}
 public LocalDateTime getHandledAt(){return handledAt;} public void setHandledAt(LocalDateTime v){handledAt=v;}
 public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime v){completedAt=v;}
 public List<String> getPhotos(){return photos;}
 public String getOrderCode(){return order!=null?"#"+order.getId():"#TK"+customCake.getId();}
 public String getOrderUrl(){return order!=null?"/orders/"+order.getId():"/custom-cakes/"+customCake.getId();}
 public String getAdminOrderUrl(){return "/admin"+getOrderUrl();}
 public String getStatusLabel(){return switch(status){case REQUESTED->"Chờ cửa hàng xem xét";case APPROVED->"Đã duyệt · đang xử lý";case REJECTED->"Đã từ chối";case COMPLETED->"Đã xử lý xong";};}
 public String getResolutionLabel(){return resolution==Resolution.REFUND?"Hoàn tiền":"Đổi bánh / làm lại";}
}
