package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="luot_su_dung_ma")
public class VoucherUsage {
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@ManyToOne(optional=false) @JoinColumn(name="ma_giam_gia_id") private Voucher voucher;
@ManyToOne(optional=false) @JoinColumn(name="nguoi_dung_id") private User user;
@OneToOne(optional=false) @JoinColumn(name="don_hang_id",unique=true) private CustomerOrder order;
public Long getId() { return id; }
public void setId(Long value) { this.id=value; }
public Voucher getVoucher() { return voucher; }
public void setVoucher(Voucher value) { this.voucher=value; }
public User getUser() { return user; }
public void setUser(User value) { this.user=value; }
public CustomerOrder getOrder() { return order; }
public void setOrder(CustomerOrder value) { this.order=value; }

}
