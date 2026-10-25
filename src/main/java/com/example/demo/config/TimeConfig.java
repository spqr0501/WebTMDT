package com.example.demo.config;
import org.springframework.context.annotation.Configuration;
@Configuration
public class TimeConfig {
 public TimeConfig(){java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));}
}
