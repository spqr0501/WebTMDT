package com.example.demo.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="ma_dat_lai_mat_khau")
public class PasswordResetToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="nguoi_dung_id") private User user;
 @Column(name="ma_bam",nullable=false,unique=true,length=64) private String hash;
 @Column(name="ngay_tao",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 @Column(name="het_han",nullable=false) private LocalDateTime expiresAt;
 @Column(name="da_dung_luc") private LocalDateTime usedAt;
 public Long getId(){return id;}
 public User getUser(){return user;}public void setUser(User v){user=v;}
 public String getHash(){return hash;}public void setHash(String v){hash=v;}
 public LocalDateTime getCreatedAt(){return createdAt;}
 public LocalDateTime getExpiresAt(){return expiresAt;}public void setExpiresAt(LocalDateTime v){expiresAt=v;}
 public LocalDateTime getUsedAt(){return usedAt;}public void setUsedAt(LocalDateTime v){usedAt=v;}
 public boolean isValid(){return usedAt==null&&expiresAt.isAfter(LocalDateTime.now());}
}
