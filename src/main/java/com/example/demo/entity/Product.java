package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="san_pham")
public class Product {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@org.hibernate.annotations.Nationalized @Column(name="ten",nullable=false,length=150) private String name;
@org.hibernate.annotations.Nationalized @Column(name="mo_ta",nullable=false,length=5000) private String description;
@Column(name="gia",nullable=false,precision=14,scale=0) private BigDecimal price;
@Column(name="ton_kho",nullable=false) private int stock = 0;
@ManyToOne(optional=false) @JoinColumn(name="danh_muc_id") private Category category;
@Column(name="duong_dan_anh",length=500) private String imageUrl = "/images/cake.svg";
@Column(name="duong_dan_video",length=500) private String videoUrl;
@Column(name="noi_bat",nullable=false) private boolean featured = false;
@Column(name="dang_ap_dung",nullable=false) private boolean active = true;
@Version @Column(name="phien_ban") private long version = 0;
public Long getId() { return id; }
public void setId(Long value) { this.id=value; }
public String getName() { return name; }
public void setName(String value) { this.name=value; }
public String getDescription() { return description; }
public void setDescription(String value) { this.description=value; }
public BigDecimal getPrice() { return price; }
public void setPrice(BigDecimal value) { this.price=value; }
public int getStock() { return stock; }
public void setStock(int value) { this.stock=value; }
public Category getCategory() { return category; }
public void setCategory(Category value) { this.category=value; }
public String getImageUrl() { return imageUrl; }
public void setImageUrl(String value) { this.imageUrl=value; }
public String getVideoUrl() { return videoUrl; }
public void setVideoUrl(String value) { this.videoUrl=value; }
public boolean getFeatured() { return featured; }
public void setFeatured(boolean value) { this.featured=value; }
public boolean getActive() { return active; }
public void setActive(boolean value) { this.active=value; }
public long getVersion() { return version; }
public void setVersion(long value) { this.version=value; }

@org.hibernate.annotations.Nationalized @Column(name="thanh_phan",length=1000) private String ingredients;
public String getIngredients() { return ingredients; }
public void setIngredients(String v) { ingredients=v; }
@org.hibernate.annotations.Nationalized @Column(name="chat_gay_di_ung",length=500) private String allergens;
public String getAllergens() { return allergens; }
public void setAllergens(String v) { allergens=v; }
@org.hibernate.annotations.Nationalized @Column(name="bao_quan",length=1000) private String storageInstructions;
public String getStorageInstructions() { return storageInstructions; }
public void setStorageInstructions(String v) { storageInstructions=v; }
@ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="anh_san_pham",joinColumns=@JoinColumn(name="san_pham_id")) @OrderColumn(name="thu_tu") @Column(name="duong_dan",nullable=false,length=500) private java.util.List<String> gallery = new java.util.ArrayList<>();
public java.util.List<String> getGallery() { return gallery; }
public void setGallery(java.util.List<String> v) { gallery=v; }
}
