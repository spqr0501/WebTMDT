package com.example.demo.entity;
public enum PaymentMethod { COD, BANK_TRANSFER, STRIPE;
 public String getLabel(){return switch(this){case COD->"Thanh toán khi nhận hàng";case BANK_TRANSFER->"Chuyển khoản ngân hàng";case STRIPE->"Stripe Sandbox · thanh toán thẻ thử";};}
}
