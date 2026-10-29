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
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(AdminBootstrap.class);
 private final UserRepository users; private final PasswordEncoder encoder;
 @Value("${app.admin.email:}") private String email;
 @Value("${app.admin.password:}") private String password;
 public AdminBootstrap(UserRepository users,PasswordEncoder encoder){this.users=users;this.encoder=encoder;}
 public void run(ApplicationArguments args){
  if(email.isBlank()||password.isBlank()||users.findByEmailIgnoreCase(email.strip()).isPresent())return;
  if(password.length()<12||password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72){
   log.warn("Không tạo tài khoản quản trị mới: ADMIN_PASSWORD cần ít nhất 12 ký tự và tối đa 72 byte UTF-8. Sửa biến này trong Run Configurations > Environment hoặc xóa ADMIN_EMAIL và ADMIN_PASSWORD nếu không cần tạo thêm admin. Website tiếp tục khởi động; tài khoản hiện có giữ nguyên.");
   return;
  }
  User u=new User();u.setName("Quản trị Bóng Bakery");u.setEmail(email.strip().toLowerCase(java.util.Locale.ROOT));u.setPassword(encoder.encode(password));u.setRole(Role.ADMIN);users.save(u);
 }
}
