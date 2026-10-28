package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="nguoi_dung")
public class User {
@Column(name="phien_xac_thuc",nullable=false) private long authVersion=0;
public long getAuthVersion(){return authVersion;}public void setAuthVersion(long v){authVersion=v;}
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@org.hibernate.annotations.Nationalized @Column(name="ten",nullable=false,length=100) private String name;
@Column(name="email",nullable=false,unique=true,length=190) private String email;
@Column(name="mat_khau",nullable=false) private String password;
@Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="vai_tro",nullable=false,length=20) private Role role = Role.USER;
@Column(name="duoc_hoat_dong",nullable=false) private boolean enabled = true;
public Long getId() { return id; }
public void setId(Long value) { this.id=value; }
public String getName() { return name; }
public void setName(String value) { this.name=value; }
public String getEmail() { return email; }
public void setEmail(String value) { this.email=value; }
public String getPassword() { return password; }
public void setPassword(String value) { this.password=value; }
public Role getRole() { return role; }
public void setRole(Role value) { this.role=value; }
public boolean getEnabled() { return enabled; }
public void setEnabled(boolean value) { this.enabled=value; }


@Column(name="dien_thoai_shipper",length=20) private String shipperPhone;
@org.hibernate.annotations.Nationalized @Column(name="phuong_tien_shipper",length=100) private String shipperVehicle;
public String getShipperPhone(){return shipperPhone;}public void setShipperPhone(String v){shipperPhone=v;}
public String getShipperVehicle(){return shipperVehicle;}public void setShipperVehicle(String v){shipperVehicle=v;}

@Column(name="so_dien_thoai",length=20) private String phone;
public String getPhone(){return phone;}public void setPhone(String v){phone=v;}
}
