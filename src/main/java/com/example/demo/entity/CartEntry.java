package com.example.demo.entity;
import jakarta.persistence.*;
@Entity @Table(name="dong_gio_hang",uniqueConstraints=@UniqueConstraint(columnNames={"nguoi_dung_id","san_pham_id","tuy_chon_id"}))
public class CartEntry {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="nguoi_dung_id") private User user;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="san_pham_id") private Product product;
 @Column(name="tuy_chon_id",nullable=false) private Long optionId=0L;
 @Column(name="so_luong",nullable=false) private int quantity;
 public Long getId(){return id;}
 public User getUser(){return user;}public void setUser(User v){user=v;}
 public Product getProduct(){return product;}public void setProduct(Product v){product=v;}
 public Long getOptionId(){return optionId;}public void setOptionId(Long v){optionId=v;}
 public int getQuantity(){return quantity;}public void setQuantity(int v){quantity=v;}
}
