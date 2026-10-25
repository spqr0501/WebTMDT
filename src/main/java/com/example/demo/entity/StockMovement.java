package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity
@Table(name="lich_su_kho")
public class StockMovement {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
public Long getId() { return id; }
public void setId(Long v) { id=v; }
@ManyToOne(optional=false) @JoinColumn(name="san_pham_id") private Product product;
public Product getProduct() { return product; }
public void setProduct(Product v) { product=v; }
@Column(name="thay_doi",nullable=false) private int delta;
public int getDelta() { return delta; }
public void setDelta(int v) { delta=v; }
@Column(name="ton_sau",nullable=false) private int balance;
public int getBalance() { return balance; }
public void setBalance(int v) { balance=v; }
@org.hibernate.annotations.Nationalized @Column(name="ly_do",nullable=false,length=300) private String reason;
public String getReason() { return reason; }
public void setReason(String v) { reason=v; }
@Column(name="nguoi_thuc_hien",nullable=false,length=190) private String actor;
public String getActor() { return actor; }
public void setActor(String v) { actor=v; }
@Column(name="ngay_tao",nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
public LocalDateTime getCreatedAt() { return createdAt; }
public void setCreatedAt(LocalDateTime v) { createdAt=v; }

}
