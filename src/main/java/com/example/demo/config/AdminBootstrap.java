package com.example.demo.config;
import com.example.demo.entity.*;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class AdminBootstrap implements ApplicationRunner {
 private final UserRepository users; private final PasswordEncoder encoder;
 @Value("${app.admin.email:}") private String email;
 @Value("${app.admin.password:}") private String password;
 public AdminBootstrap(UserRepository users,PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
 public void run(ApplicationArguments args){
  if(email.isBlank()||password.isBlank()||users.findByEmailIgnoreCase(email).isPresent())return;
  if(password.length()<12||password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new IllegalArgumentException("ADMIN_PASSWORD cần 12 ký tự trở lên và tối đa 72 byte.");
  User u=new User();u.setName("Quản trị Bóng Bakery");u.setEmail(email.strip().toLowerCase(java.util.Locale.ROOT));u.setPassword(encoder.encode(password));u.setRole(Role.ADMIN);users.save(u);
 }
}
