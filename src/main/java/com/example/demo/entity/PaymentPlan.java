package com.example.demo.entity;
public enum PaymentPlan {
 ON_RECEIPT("Thanh toán khi nhận bánh"), DEPOSIT("Đặt cọc 50%"), FULL("Thanh toán đủ trước");
 private final String label;
 PaymentPlan(String label){this.label=label;}
 public String getLabel(){return label;}
}
