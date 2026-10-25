package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="phu_kien")
public class Accessory {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
public Long getId(){return id;} public void setId(Long value){this.id=value;}
@org.hibernate.annotations.Nationalized @Column(name="ten",nullable=false,length=100) private String name;
public String getName(){return name;} public void setName(String value){this.name=value;}
@Column(name="gia",nullable=false,precision=14,scale=0) private BigDecimal price = BigDecimal.ZERO;
public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal value){this.price=value;}
@Column(name="ton_kho",nullable=false) private int stock = 0;
public int getStock(){return stock;} public void setStock(int value){this.stock=value;}
@Column(name="dang_ban",nullable=false) private boolean active = true;
public boolean getActive(){return active;} public void setActive(boolean value){this.active=value;}
@Version @Column(name="phien_ban",nullable=false) private long version = 0;
public long getVersion(){return version;} public void setVersion(long value){this.version=value;}

}
