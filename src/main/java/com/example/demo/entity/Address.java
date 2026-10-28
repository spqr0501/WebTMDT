package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="dia_chi")
public class Address {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@ManyToOne(optional=false) @JoinColumn(name="nguoi_dung_id") private User user;
@org.hibernate.annotations.Nationalized @Column(name="nguoi_nhan",nullable=false,length=100) private String recipient;
@Column(name="so_dien_thoai",nullable=false,length=20) private String phone;
@org.hibernate.annotations.Nationalized @Column(name="dia_chi_chi_tiet",nullable=false,length=500) private String detail;
public Long getId() { return id; }
public void setId(Long value) { this.id=value; }
public User getUser() { return user; }
public void setUser(User value) { this.user=value; }
public String getRecipient() { return recipient; }
public void setRecipient(String value) { this.recipient=value; }
public String getPhone() { return phone; }
public void setPhone(String value) { this.phone=value; }
public String getDetail() { return detail; }
public void setDetail(String value) { this.detail=value; }


@Column(name="vi_do") private Double latitude;
@Column(name="kinh_do") private Double longitude;
public Double getLatitude(){return latitude;}public void setLatitude(Double v){latitude=v;}
public Double getLongitude(){return longitude;}public void setLongitude(Double v){longitude=v;}

}
