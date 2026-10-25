package com.example.demo.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name="don_hang")
public class CustomerOrder {
@Column(name="ngay_hoan_thanh") private LocalDateTime completedAt;
public LocalDateTime getCompletedAt(){return completedAt;} public void setCompletedAt(LocalDateTime v){completedAt=v;}
@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
@ManyToOne(optional=false) @JoinColumn(name="nguoi_dung_id") private User user;
@Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="trang_thai",nullable=false,length=30) private OrderStatus status = OrderStatus.PENDING;
@Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="phuong_thuc_thanh_toan",nullable=false,length=20) private PaymentMethod paymentMethod;
@org.hibernate.annotations.Nationalized @Column(name="nguoi_nhan",nullable=false,length=100) private String recipient;
@Column(name="so_dien_thoai",nullable=false,length=20) private String phone;
@org.hibernate.annotations.Nationalized @Column(name="dia_chi_giao_hang",nullable=false,length=500) private String deliveryAddress;
@org.hibernate.annotations.Nationalized @Column(name="ghi_chu",length=500) private String note;
@Column(name="tien_hang",nullable=false,precision=14,scale=0) private BigDecimal subtotal;
@Column(name="tien_giam",nullable=false,precision=14,scale=0) private BigDecimal discount = BigDecimal.ZERO;
@Column(name="phi_giao_hang",nullable=false,precision=14,scale=0) private BigDecimal shippingFee = BigDecimal.ZERO;
@Column(name="tong_thanh_toan",nullable=false,precision=14,scale=0) private BigDecimal total;
@Column(name="ma_uu_dai",length=40) private String voucherCode;
@Column(name="ma_chong_dat_trung",nullable=false,unique=true,length=36) private String checkoutToken;
@Column(name="ngay_tao",nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
@OneToMany(mappedBy="order",cascade=CascadeType.ALL,fetch=FetchType.EAGER) private java.util.List<OrderItem> items = new java.util.ArrayList<>();
public Long getId() { return id; }
public void setId(Long value) { this.id=value; }
public User getUser() { return user; }
public void setUser(User value) { this.user=value; }
public OrderStatus getStatus() { return status; }
public void setStatus(OrderStatus value) { this.status=value; }
public PaymentMethod getPaymentMethod() { return paymentMethod; }
public void setPaymentMethod(PaymentMethod value) { this.paymentMethod=value; }
public String getRecipient() { return recipient; }
public void setRecipient(String value) { this.recipient=value; }
public String getPhone() { return phone; }
public void setPhone(String value) { this.phone=value; }
public String getDeliveryAddress() { return deliveryAddress; }
public void setDeliveryAddress(String value) { this.deliveryAddress=value; }
public String getNote() { return note; }
public void setNote(String value) { this.note=value; }
public BigDecimal getSubtotal() { return subtotal; }
public void setSubtotal(BigDecimal value) { this.subtotal=value; }
public BigDecimal getDiscount() { return discount; }
public void setDiscount(BigDecimal value) { this.discount=value; }
public BigDecimal getShippingFee() { return shippingFee; }
public void setShippingFee(BigDecimal value) { this.shippingFee=value; }
public BigDecimal getTotal() { return total; }
public void setTotal(BigDecimal value) { this.total=value; }
public String getVoucherCode() { return voucherCode; }
public void setVoucherCode(String value) { this.voucherCode=value; }
public String getCheckoutToken() { return checkoutToken; }
public void setCheckoutToken(String value) { this.checkoutToken=value; }
public LocalDateTime getCreatedAt() { return createdAt; }
public void setCreatedAt(LocalDateTime value) { this.createdAt=value; }
public java.util.List<OrderItem> getItems() { return items; }
public void setItems(java.util.List<OrderItem> value) { this.items=value; }

@Column(name="ngay_giao") private java.time.LocalDate deliveryDate;
public java.time.LocalDate getDeliveryDate() { return deliveryDate; }
public void setDeliveryDate(java.time.LocalDate v) { deliveryDate=v; }
@Column(name="khung_gio",length=20) private String deliverySlot;
public String getDeliverySlot() { return deliverySlot; }
public void setDeliverySlot(String v) { deliverySlot=v; }
@org.hibernate.annotations.Nationalized @Column(name="loi_chuc",length=100) private String cakeMessage;
public String getCakeMessage() { return cakeMessage; }
public void setCakeMessage(String v) { cakeMessage=v; }
@Enumerated(EnumType.STRING) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.VARCHAR) @Column(name="hinh_thuc_nhan",nullable=false,length=30) private FulfillmentMethod fulfillmentMethod = FulfillmentMethod.DELIVERY;
public FulfillmentMethod getFulfillmentMethod(){return fulfillmentMethod;} public void setFulfillmentMethod(FulfillmentMethod value){this.fulfillmentMethod=value;}
@Column(name="dat_truoc",nullable=false) private boolean preorder = false;
public boolean getPreorder(){return preorder;} public void setPreorder(boolean value){this.preorder=value;}
@OneToMany(mappedBy="order",cascade=CascadeType.ALL,fetch=FetchType.EAGER)
@org.hibernate.annotations.Fetch(org.hibernate.annotations.FetchMode.SELECT)
private java.util.Set<OrderAccessory> accessories = new java.util.LinkedHashSet<>();
public java.util.Set<OrderAccessory> getAccessories(){return accessories;} public void setAccessories(java.util.Set<OrderAccessory> value){this.accessories=value;}

 public java.util.List<OrderStatus> getProgressSteps(){return fulfillmentMethod==FulfillmentMethod.PICKUP?java.util.List.of(OrderStatus.PENDING,OrderStatus.CONFIRMED,OrderStatus.PREPARING,OrderStatus.READY_FOR_PICKUP,OrderStatus.COMPLETED):java.util.List.of(OrderStatus.PENDING,OrderStatus.CONFIRMED,OrderStatus.PREPARING,OrderStatus.SHIPPING,OrderStatus.COMPLETED);}
 public java.util.List<OrderStatus> getNextStatuses(){return java.util.Arrays.stream(OrderStatus.values()).filter(this::canMoveTo).toList();}
 public boolean canMoveTo(OrderStatus next){if(!status.canMoveTo(next))return false;return !(fulfillmentMethod==FulfillmentMethod.PICKUP&&next==OrderStatus.SHIPPING)&&!(fulfillmentMethod==FulfillmentMethod.DELIVERY&&next==OrderStatus.READY_FOR_PICKUP);}


@Embedded private PaymentSummary payment=new PaymentSummary();
public PaymentSummary getPayment(){return payment;}public void setPayment(PaymentSummary value){payment=value;}
}
