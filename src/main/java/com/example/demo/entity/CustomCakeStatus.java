package com.example.demo.entity;
public enum CustomCakeStatus {
 SENT("Chờ báo giá"),QUOTED("Đã báo giá"),ACCEPTED("Khách đã xác nhận"),PREPARING("Đang chuẩn bị"),READY("Sẵn sàng giao / nhận"),COMPLETED("Hoàn thành"),REJECTED("Không thể đáp ứng"),CANCELLED("Đã hủy");
 private final String label;CustomCakeStatus(String label){this.label=label;}public String getLabel(){return label;}
}
