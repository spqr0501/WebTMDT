package com.example.demo.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="phieu_ho_tro")
public class SupportTicket {
 public enum Status { OPEN, IN_PROGRESS, RESOLVED }
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="nguoi_dung_id") private User user;
 @org.hibernate.annotations.Nationalized @Column(name="chu_de",nullable=false,length=150) private String subject;
 @org.hibernate.annotations.Nationalized @Column(name="hoi_thoai_chia_se",length=6000) private String transcript;
 @Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="trang_thai",nullable=false,length=30) private Status status=Status.OPEN;
 @Column(name="nhan_vien",length=190) private String assignee;
 @Column(name="ma_chong_lap",nullable=false,unique=true,length=36) private String token;
 @Column(name="ngay_tao",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 @Column(name="ngay_cap_nhat",nullable=false) private LocalDateTime updatedAt=LocalDateTime.now();
 public Long getId(){return id;}
 public User getUser(){return user;} public void setUser(User v){user=v;}
 public String getSubject(){return subject;} public void setSubject(String v){subject=v;}
 public String getTranscript(){return transcript;} public void setTranscript(String v){transcript=v;}
 public Status getStatus(){return status;} public void setStatus(Status v){status=v;}
 public String getAssignee(){return assignee;} public void setAssignee(String v){assignee=v;}
 public String getToken(){return token;} public void setToken(String v){token=v;}
 public LocalDateTime getCreatedAt(){return createdAt;}
 public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
 public String getStatusLabel(){return switch(status){case OPEN->"Chờ nhân viên tiếp nhận";case IN_PROGRESS->"Nhân viên đang xử lý";case RESOLVED->"Đã giải quyết";};}
}
