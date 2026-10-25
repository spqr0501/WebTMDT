package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="phu_kien_don_hang")
public class OrderAccessory {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
public Long getId(){return id;} public void setId(Long value){this.id=value;}
@ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="don_hang_id") private CustomerOrder order;
public CustomerOrder getOrder(){return order;} public void setOrder(CustomerOrder value){this.order=value;}
@ManyToOne(optional=false) @JoinColumn(name="phu_kien_id") private Accessory accessory;
public Accessory getAccessory(){return accessory;} public void setAccessory(Accessory value){this.accessory=value;}
@org.hibernate.annotations.Nationalized @Column(name="ten",nullable=false,length=100) private String name;
public String getName(){return name;} public void setName(String value){this.name=value;}
@Column(name="don_gia",nullable=false,precision=14,scale=0) private BigDecimal unitPrice;
public BigDecimal getUnitPrice(){return unitPrice;} public void setUnitPrice(BigDecimal value){this.unitPrice=value;}
@Column(name="so_luong",nullable=false) private int quantity = 0;
public int getQuantity(){return quantity;} public void setQuantity(int value){this.quantity=value;}
public BigDecimal getLineTotal(){return unitPrice.multiply(BigDecimal.valueOf(quantity));}
}
