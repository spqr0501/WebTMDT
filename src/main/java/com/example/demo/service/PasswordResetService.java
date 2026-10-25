package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

@Service
public class PasswordResetService {
 private final PasswordResetRepository tokens;private final UserRepository users;private final PasswordEncoder encoder;private final ObjectProvider<JavaMailSender> mail;
 @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
 @Value("${app.mail.enabled:false}") private boolean enabled;
 @Value("${app.mail.from:}") private String from;
 @Value("${app.public-base-url:http://localhost:8080}") private String baseUrl;
 private final Map<String,List<Instant>> limits=new HashMap<>();private final SecureRandom random=new SecureRandom();
 public PasswordResetService(PasswordResetRepository tokens,UserRepository users,PasswordEncoder encoder,ObjectProvider<JavaMailSender> mail){this.tokens=tokens;this.users=users;this.encoder=encoder;this.mail=mail;}
 public boolean ready(){return enabled&&!from.isBlank()&&mail.getIfAvailable()!=null;}
 private String hash(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 private synchronized boolean allowed(String ip){Instant cutoff=Instant.now().minusSeconds(900);limits.entrySet().removeIf(e->e.getValue().stream().noneMatch(t->t.isAfter(cutoff)));String key=hash(ip==null?"unknown":ip);if(!limits.containsKey(key)&&limits.size()>=10000)return false;var times=limits.computeIfAbsent(key,k->new ArrayList<>());times.removeIf(t->!t.isAfter(cutoff));if(times.size()>=10)return false;times.add(Instant.now());return true;}
 @Transactional public void request(String email,String ip){
  if(!ready())throw new BusinessException("Chức năng khôi phục qua email đang tạm chưa sẵn sàng. Vui lòng liên hệ cửa hàng.");
  if(!allowed(ip))return;
  var found=users.findByEmailIgnoreCase(email.strip());if(found.isEmpty()||!found.get().getEnabled())return;
  var u=users.lockById(found.get().getId()).orElseThrow();entityManager.refresh(u,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
  if(tokens.existsByUserIdAndCreatedAtAfter(u.getId(),LocalDateTime.now().minusMinutes(1)))return;
  java.net.URI base;try{base=java.net.URI.create(baseUrl);if(base.getHost()==null||base.getUserInfo()!=null||base.getQuery()!=null||base.getFragment()!=null||!Set.of("http","https").contains(base.getScheme()))throw new IllegalArgumentException();}catch(IllegalArgumentException e){throw new BusinessException("Chức năng khôi phục đang tạm gián đoạn. Vui lòng liên hệ cửa hàng.");}
  byte[] secret=new byte[32];random.nextBytes(secret);String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(secret);tokens.invalidate(u.getId(),LocalDateTime.now());
  var token=new PasswordResetToken();token.setUser(u);token.setHash(hash(raw));token.setExpiresAt(LocalDateTime.now().plusMinutes(15));tokens.saveAndFlush(token);
  var message=new SimpleMailMessage();message.setFrom(from);message.setTo(u.getEmail());message.setSubject("Bóng Bakery — đặt lại mật khẩu");message.setText("Bạn đã yêu cầu đặt lại mật khẩu Bóng Bakery.\nLiên kết dùng một lần, hết hạn sau 15 phút:\n"+baseUrl.replaceAll("/+$","")+"/reset-password?token="+raw+"\n\nNếu không yêu cầu, bạn có thể bỏ qua thư này. Không chia sẻ liên kết cho người khác.");
  try{mail.getObject().send(message);}catch(MailException e){org.slf4j.LoggerFactory.getLogger(PasswordResetService.class).warn("Gửi email khôi phục thất bại. Kiểm tra cấu hình SMTP; không ghi nội dung hoặc liên kết khôi phục vào log.");}
 }
 @Transactional(readOnly=true) public boolean valid(String raw){return raw!=null&&raw.matches("[A-Za-z0-9_-]{43}")&&tokens.findByHash(hash(raw)).filter(t->t.isValid()&&t.getUser().getEnabled()).isPresent();}
 @Transactional public void reset(String raw,String password,String confirmation){
  if(raw==null||!raw.matches("[A-Za-z0-9_-]{43}"))throw invalid();
  var token=tokens.findByHash(hash(raw)).orElseThrow(this::invalid);var u=users.lockById(token.getUser().getId()).orElseThrow(this::invalid);entityManager.refresh(u,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);entityManager.refresh(token,jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
  if(!token.isValid()||!u.getEnabled())throw invalid();
  if(password==null||password.length()<8||password.length()>72||password.getBytes(StandardCharsets.UTF_8).length>72)throw new BusinessException("Mật khẩu mới cần từ 8 ký tự và tối đa 72 byte.");
  if(!password.equals(confirmation))throw new BusinessException("Mật khẩu nhập lại chưa khớp.");
  u.setPassword(encoder.encode(password));u.setAuthVersion(Math.addExact(u.getAuthVersion(),1));tokens.invalidate(u.getId(),LocalDateTime.now());
 }
 private BusinessException invalid(){return new BusinessException("Liên kết không hợp lệ, đã dùng hoặc đã hết hạn. Hãy yêu cầu liên kết mới.");}
}
