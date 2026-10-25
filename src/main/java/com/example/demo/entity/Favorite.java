package com.example.demo.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="banh_yeu_thich",uniqueConstraints=@UniqueConstraint(columnNames={"nguoi_dung_id","san_pham_id"}))
public class Favorite {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="nguoi_dung_id") private User user;
 @ManyToOne(optional=false) @JoinColumn(name="san_pham_id") private Product product;
 @Column(name="ngay_them",nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;}
 public User getUser(){return user;}public void setUser(User v){user=v;}
 public Product getProduct(){return product;}public void setProduct(Product v){product=v;}
 public LocalDateTime getCreatedAt(){return createdAt;}
}
