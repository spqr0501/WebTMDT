package com.example.demo.entity;
public enum FulfillmentMethod {
 DELIVERY("Giao tận nơi"), PICKUP("Nhận tại cửa hàng");
 private final String label;FulfillmentMethod(String label){this.label=label;}public String getLabel(){return label;}
}
