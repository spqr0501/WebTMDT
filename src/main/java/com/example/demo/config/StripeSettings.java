package com.example.demo.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
@Component
public class StripeSettings {
 @Value("${app.stripe.enabled:false}") private boolean enabled;
 @Value("${app.stripe.secret-key:}") private String secret;
 @Value("${app.stripe.webhook-secret:}") private String webhookSecret;
 @Value("${app.stripe.base-url:http://localhost:8080}") private String baseUrl;
 public String secret(){return secret;}public String webhookSecret(){return webhookSecret;}
 public String baseUrl(){return baseUrl.replaceAll("/+$","");}
 public boolean webhookReady(){return webhookSecret!=null&&webhookSecret.startsWith("whsec_");}
 public boolean ready(){
  if(!enabled||secret==null||!(secret.startsWith("sk_test_")||secret.startsWith("rk_test_"))||!webhookReady())return false;
  try{var u=URI.create(baseUrl);return u.getHost()!=null&&u.getUserInfo()==null&&u.getQuery()==null&&u.getFragment()==null&&(u.getPath().isEmpty()||u.getPath().equals("/"))&&("https".equals(u.getScheme())||("http".equals(u.getScheme())&&java.util.Set.of("localhost","127.0.0.1","[::1]").contains(u.getHost())));}catch(Exception e){return false;}
 }
}
