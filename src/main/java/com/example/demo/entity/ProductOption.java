package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity
@Table(name="tuy_chon_banh")
public class ProductOption {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
public Long getId() { return id; }
public void setId(Long v) { id=v; }
@ManyToOne(optional=false) @JoinColumn(name="san_pham_id") private Product product;
public Product getProduct() { return product; }
public void setProduct(Product v) { product=v; }
@org.hibernate.annotations.Nationalized @Column(name="kich_thuoc",nullable=false,length=50) private String sizeLabel;
public String getSizeLabel() { return sizeLabel; }
public void setSizeLabel(String v) { sizeLabel=v; }
@org.hibernate.annotations.Nationalized @Column(name="huong_vi",nullable=false,length=80) private String flavor;
public String getFlavor() { return flavor; }
public void setFlavor(String v) { flavor=v; }
@Column(name="gia_them",nullable=false,precision=14,scale=0) private BigDecimal extraPrice = BigDecimal.ZERO;
public BigDecimal getExtraPrice() { return extraPrice; }
public void setExtraPrice(BigDecimal v) { extraPrice=v; }
@Column(name="dang_ban",nullable=false) private boolean active = true;
public boolean getActive() { return active; }
public void setActive(boolean v) { active=v; }
public String getLabel(){return sizeLabel+" · "+flavor;} public BigDecimal getPrice(){return product.getPrice().add(extraPrice);}
}
