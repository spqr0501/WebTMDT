package com.example.demo.service;
public final class BakeryClock {
 public static final java.time.ZoneId ZONE=java.time.ZoneId.of("Asia/Ho_Chi_Minh");
 private BakeryClock(){}
 public static java.time.LocalDate today(){return java.time.LocalDate.now(ZONE);}
 public static java.time.LocalDateTime now(){return java.time.LocalDateTime.now(ZONE);}
}
