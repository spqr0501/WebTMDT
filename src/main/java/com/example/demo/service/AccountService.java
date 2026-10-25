package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.dto.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.List;

@Service
public class AccountService {
 private final UserRepository users; private final AddressRepository addresses; private final PasswordEncoder encoder;
 public AccountService(UserRepository users, AddressRepository addresses, PasswordEncoder encoder) {this.users=users;this.addresses=addresses;this.encoder=encoder;}
 public User current(String email) {User u=users.findByEmailIgnoreCase(email).orElseThrow(()->new BusinessException("Tài khoản không tồn tại.")); if(!u.getEnabled())throw new BusinessException("Tài khoản đã bị khóa.");return u;}
 @Transactional public void register(RegistrationForm f) {
  if(f.getPassword()==null||f.getConfirmPassword()==null||!f.getPassword().equals(f.getConfirmPassword()))throw new BusinessException("Mật khẩu nhập lại chưa khớp.");
  String email=f.getEmail().strip().toLowerCase(Locale.ROOT);
  if(users.findByEmailIgnoreCase(email).isPresent())throw new BusinessException("Email đã được đăng ký.");
  if(f.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new BusinessException("Mật khẩu tối đa 72 byte UTF-8.");
  User u=new User();u.setName(f.getName().strip());u.setEmail(email);u.setPassword(encoder.encode(f.getPassword()));users.save(u);
 }
 public List<Address> addresses(String email){return addresses.findByUserIdOrderByIdDesc(current(email).getId());}
 @Transactional public void addAddress(String email,AddressForm f){Address a=new Address();a.setUser(current(email));a.setRecipient(f.getRecipient());a.setPhone(f.getPhone());a.setDetail(f.getDetail());addresses.save(a);}
 @Transactional public void setEnabled(Long id,boolean enabled,String actor) {
  User u=users.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy tài khoản."));
  if(u.getRole()==Role.ADMIN)throw new BusinessException("Không khóa tài khoản quản trị trong màn hình này.");
  u.setEnabled(enabled);
 }
}
