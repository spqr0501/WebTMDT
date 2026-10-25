package com.example.demo.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="tin_nhan_ho_tro")
public class SupportMessage {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="phieu_id") private SupportTicket ticket;
 @Column(name="tu_nhan_vien",nullable=false) private boolean staff;
 @org.hibernate.annotations.Nationalized @Column(name="noi_dung",nullable=false,length=2000) private String content;
 @Column(name="nguoi_gui",nullable=false,length=190) private String actor;
 @Column(name="ngay_gui",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;}
 public SupportTicket getTicket(){return ticket;} public void setTicket(SupportTicket v){ticket=v;}
 public boolean getStaff(){return staff;} public void setStaff(boolean v){staff=v;}
 public String getContent(){return content;} public void setContent(String v){content=v;}
 public String getActor(){return actor;} public void setActor(String v){actor=v;}
 public LocalDateTime getCreatedAt(){return createdAt;}
}
