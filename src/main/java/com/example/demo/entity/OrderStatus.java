package com.example.demo.entity;
public enum OrderStatus {
 PENDING("Chờ xác nhận"), CONFIRMED("Đã xác nhận"), PREPARING("Đang chuẩn bị"), SHIPPING("Đang giao"), READY_FOR_PICKUP("Sẵn sàng nhận bánh"), COMPLETED("Hoàn thành"), CANCELLED("Đã hủy");
 private final String label; OrderStatus(String label) { this.label=label; } public String getLabel(){return label;}
 public boolean canMoveTo(OrderStatus next) { return (this==PENDING && (next==CONFIRMED || next==CANCELLED)) || (this==CONFIRMED && next==PREPARING) || (this==PREPARING && (next==SHIPPING || next==READY_FOR_PICKUP)) || ((this==SHIPPING || this==READY_FOR_PICKUP) && next==COMPLETED); }
}
