package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity
@Table(name="danh_gia",uniqueConstraints=@UniqueConstraint(name="uq_danh_gia_nguoi_san_pham",columnNames={"san_pham_id","nguoi_dung_id"}))
public class Review {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
public Long getId() { return id; }
public void setId(Long v) { id=v; }
@ManyToOne(optional=false) @JoinColumn(name="san_pham_id") private Product product;
public Product getProduct() { return product; }
public void setProduct(Product v) { product=v; }
@ManyToOne(optional=false) @JoinColumn(name="nguoi_dung_id") private User user;
public User getUser() { return user; }
public void setUser(User v) { user=v; }
@Column(name="so_sao",nullable=false) private int rating;
public int getRating() { return rating; }
public void setRating(int v) { rating=v; }
@org.hibernate.annotations.Nationalized @Column(name="noi_dung",nullable=false,length=2000) private String comment;
public String getComment() { return comment; }
public void setComment(String v) { comment=v; }
@Column(name="anh_1",length=500) private String photo1;
public String getPhoto1() { return photo1; }
public void setPhoto1(String v) { photo1=v; }
@Column(name="anh_2",length=500) private String photo2;
public String getPhoto2() { return photo2; }
public void setPhoto2(String v) { photo2=v; }
@Column(name="anh_3",length=500) private String photo3;
public String getPhoto3() { return photo3; }
public void setPhoto3(String v) { photo3=v; }
@Column(name="hien_thi",nullable=false) private boolean visible = true;
public boolean getVisible() { return visible; }
public void setVisible(boolean v) { visible=v; }
@org.hibernate.annotations.Nationalized @Column(name="ly_do_an",length=300) private String moderationReason;
public String getModerationReason() { return moderationReason; }
public void setModerationReason(String v) { moderationReason=v; }
@Column(name="ngay_tao",nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
public LocalDateTime getCreatedAt() { return createdAt; }
public void setCreatedAt(LocalDateTime v) { createdAt=v; }
public java.util.List<String> getPhotos(){return java.util.stream.Stream.of(photo1,photo2,photo3).filter(java.util.Objects::nonNull).toList();}
}
