package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="danh_muc")
public class Category {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@org.hibernate.annotations.Nationalized @Column(name="ten",nullable=false,unique=true,length=100) private String name;
@org.hibernate.annotations.Nationalized @Column(name="mo_ta",length=1000) private String description;
public Long getId() { return id; }
public void setId(Long value) { this.id=value; }
public String getName() { return name; }
public void setName(String value) { this.name=value; }
public String getDescription() { return description; }
public void setDescription(String value) { this.description=value; }

}
