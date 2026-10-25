package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="thong_bao")
public class Notification {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
public Long getId(){return id;} public void setId(Long value){this.id=value;}
@ManyToOne(optional=false) @JoinColumn(name="nguoi_dung_id") private User user;
public User getUser(){return user;} public void setUser(User value){this.user=value;}
@org.hibernate.annotations.Nationalized @Column(name="tieu_de",nullable=false,length=180) private String title;
public String getTitle(){return title;} public void setTitle(String value){this.title=value;}
@org.hibernate.annotations.Nationalized @Column(name="noi_dung",nullable=false,length=1500) private String message;
public String getMessage(){return message;} public void setMessage(String value){this.message=value;}
@Column(name="lien_ket",nullable=false,length=200) private String url;
public String getUrl(){return url;} public void setUrl(String value){this.url=value;}
@Column(name="da_doc",nullable=false) private boolean read = false;
public boolean getRead(){return read;} public void setRead(boolean value){this.read=value;}
@Column(name="ngay_tao",nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime value){this.createdAt=value;}

}
