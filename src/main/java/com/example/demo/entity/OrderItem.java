package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="chi_tiet_don_hang")
public class OrderItem {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="don_hang_id") private CustomerOrder order;
@ManyToOne(optional=false) @JoinColumn(name="san_pham_id") private Product product;
@org.hibernate.annotations.Nationalized @Column(name="ten_san_pham",nullable=false,length=150) private String productName;
@Column(name="don_gia",nullable=false,precision=14,scale=0) private BigDecimal unitPrice;
@Column(name="so_luong",nullable=false) private int quantity = 0;
public Long getId() { return id; }
public void setId(Long value) { this.id=value; }
public CustomerOrder getOrder() { return order; }
public void setOrder(CustomerOrder value) { this.order=value; }
public Product getProduct() { return product; }
public void setProduct(Product value) { this.product=value; }
public String getProductName() { return productName; }
public void setProductName(String value) { this.productName=value; }
public BigDecimal getUnitPrice() { return unitPrice; }
public void setUnitPrice(BigDecimal value) { this.unitPrice=value; }
public int getQuantity() { return quantity; }
public void setQuantity(int value) { this.quantity=value; }
public BigDecimal getLineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
@org.hibernate.annotations.Nationalized @Column(name="tuy_chon",length=150) private String optionLabel;
public String getOptionLabel() { return optionLabel; }
public void setOptionLabel(String v) { optionLabel=v; }
@Column(name="tuy_chon_id") private Long optionId;
public Long getOptionId(){return optionId;} public void setOptionId(Long value){this.optionId=value;}

}
