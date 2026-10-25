package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Mỗi lần gửi mẫu tạo một phiên bản riêng; không ghi đè mẫu khách đã duyệt. */
@Entity @Table(name="mau_banh_thiet_ke")
public class CakeDesign {
 public enum Status { PENDING, APPROVED, CHANGES_REQUESTED }
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="banh_rieng_id") private CustomCakeRequest request;
 @Column(name="anh_mau",nullable=false,length=500) private String image;
 @org.hibernate.annotations.Nationalized @Column(name="noi_dung",nullable=false,length=1500) private String description;
 @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR)
 @Column(name="trang_thai",nullable=false,length=30) private Status status=Status.PENDING;
 @org.hibernate.annotations.Nationalized @Column(name="y_kien_khach",length=1000) private String feedback;
 @Column(name="nguoi_gui",nullable=false,length=190) private String actor;
 @Column(name="ngay_gui",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 @Column(name="ngay_phan_hoi") private LocalDateTime respondedAt;
 public Long getId(){return id;}
 public CustomCakeRequest getRequest(){return request;} public void setRequest(CustomCakeRequest v){request=v;}
 public String getImage(){return image;} public void setImage(String v){image=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public Status getStatus(){return status;} public void setStatus(Status v){status=v;}
 public String getFeedback(){return feedback;} public void setFeedback(String v){feedback=v;}
 public String getActor(){return actor;} public void setActor(String v){actor=v;}
 public LocalDateTime getCreatedAt(){return createdAt;}
 public LocalDateTime getRespondedAt(){return respondedAt;} public void setRespondedAt(LocalDateTime v){respondedAt=v;}
 public String getStatusLabel(){return switch(status){case PENDING->"Chờ khách duyệt mẫu";case APPROVED->"Khách đã duyệt mẫu";case CHANGES_REQUESTED->"Khách đề nghị chỉnh sửa";};}
}
