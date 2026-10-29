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
 @org.springframework.beans.factory.annotation.Autowired private jakarta.validation.Validator validator;
 private void validateAddress(AddressForm f){if(f==null||!validator.validate(f).isEmpty())throw new BusinessException("Kiểm tra người nhận, số di động Việt Nam 10 số và địa chỉ giao bánh (tối đa 500 ký tự).");GeoService.validate(f.getLatitude(),f.getLongitude());}
 @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
 @org.springframework.beans.factory.annotation.Autowired private PasswordResetRepository resetTokens;
 @org.springframework.beans.factory.annotation.Autowired private CustomerOrderRepository deliveryOrders;
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
 @Transactional public void addAddress(String email,AddressForm f){
  validateAddress(f);var u=users.lockById(current(email).getId()).orElseThrow();var saved=addresses.findByUserIdOrderByIdDesc(u.getId());
  if(saved.size()>=20)throw new BusinessException("Sổ địa chỉ tối đa 20 địa chỉ. Hãy sửa hoặc xóa địa chỉ không dùng.");
  boolean makeDefault=f.getDefaultAddress()||saved.isEmpty();if(makeDefault)addresses.clearDefaults(u.getId());
  Address a=new Address();a.setUser(u);a.setRecipient(f.getRecipient().strip());a.setPhone(f.getPhone());a.setDetail(f.getDetail().strip());a.setLatitude(f.getLatitude());a.setLongitude(f.getLongitude());a.setDefaultAddress(makeDefault);addresses.saveAndFlush(a);
 }
 @Transactional public void setEnabled(Long id,boolean enabled,String actor) {
  User u=users.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy tài khoản."));
  if(u.getRole()==Role.ADMIN)throw new BusinessException("Không khóa tài khoản quản trị trong màn hình này.");
  if(u.getRole()==Role.SHIPPER&&!enabled&&deliveryOrders.existsByShipperIdAndStatusIn(u.getId(),java.util.List.of(OrderStatus.CONFIRMED,OrderStatus.PREPARING,OrderStatus.SHIPPING)))throw new BusinessException("Hãy phân công lại các đơn của shipper trước khi khóa tài khoản.");
  u.setEnabled(enabled);
 }

 @Transactional public void updateProfile(String email,ProfileForm f){var u=users.lockById(current(email).getId()).orElseThrow();entityManager.refresh(u,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);u.setName(f.getName().strip());u.setPhone(f.getPhone()==null||f.getPhone().isBlank()?null:f.getPhone().strip());}
 public Address ownedAddress(String email,Long id){return addresses.findByIdAndUserId(id,current(email).getId()).orElseThrow(()->new BusinessException("Không tìm thấy địa chỉ của bạn."));}
 @Transactional public void saveAddress(String email,Long id,AddressForm f){
  validateAddress(f);var u=users.lockById(current(email).getId()).orElseThrow();var a=ownedAddress(email,id);
  boolean makeDefault=f.getDefaultAddress()||a.getDefaultAddress();if(makeDefault){addresses.clearDefaults(u.getId());entityManager.refresh(a);a.setDefaultAddress(true);}
  a.setRecipient(f.getRecipient().strip());a.setPhone(f.getPhone());a.setDetail(f.getDetail().strip());a.setLatitude(f.getLatitude());a.setLongitude(f.getLongitude());addresses.saveAndFlush(a);
 }
 @Transactional public void setDefaultAddress(String email,Long id){var u=users.lockById(current(email).getId()).orElseThrow();var a=ownedAddress(email,id);addresses.clearDefaults(u.getId());entityManager.refresh(a);a.setDefaultAddress(true);addresses.saveAndFlush(a);}
 @Transactional public void deleteAddress(String email,Long id){var u=users.lockById(current(email).getId()).orElseThrow();var a=ownedAddress(email,id);boolean wasDefault=a.getDefaultAddress();addresses.delete(a);addresses.flush();if(wasDefault){var remaining=addresses.findByUserIdOrderByIdDesc(u.getId());if(!remaining.isEmpty()){remaining.get(0).setDefaultAddress(true);addresses.saveAndFlush(remaining.get(0));}}}
 @Transactional public void changePassword(String email,PasswordChangeForm f){var u=users.lockById(current(email).getId()).orElseThrow();entityManager.refresh(u,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);if(f.getCurrentPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72||!encoder.matches(f.getCurrentPassword(),u.getPassword()))throw new BusinessException("Mật khẩu hiện tại chưa đúng.");if(!f.getNewPassword().equals(f.getConfirmation()))throw new BusinessException("Mật khẩu nhập lại chưa khớp.");if(f.getNewPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new BusinessException("Mật khẩu mới tối đa 72 byte UTF-8.");if(encoder.matches(f.getNewPassword(),u.getPassword()))throw new BusinessException("Mật khẩu mới cần khác mật khẩu hiện tại.");u.setPassword(encoder.encode(f.getNewPassword()));u.setAuthVersion(Math.addExact(u.getAuthVersion(),1));resetTokens.invalidate(u.getId(),java.time.LocalDateTime.now());}
}
