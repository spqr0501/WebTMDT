package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;

@Service
public class NotificationService {
 private final NotificationRepository notifications;
 private final UserRepository users;
 private final AccountService accounts;
 public NotificationService(NotificationRepository notifications,UserRepository users,AccountService accounts){this.notifications=notifications;this.users=users;this.accounts=accounts;}
 @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
 public void send(User user,String title,String message,String url){
  Notification n=new Notification();n.setUser(user);n.setTitle(title);n.setMessage(message.length()>1500?message.substring(0,1499)+"…":message);n.setUrl(url);notifications.save(n);
 }
 @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
 public void admins(String title,String message,String url){for(User u:users.findByRoleAndEnabledTrue(Role.ADMIN))send(u,title,message,url);}
 public long unread(String email){return notifications.countByUserIdAndReadFalse(accounts.current(email).getId());}
 public Page<Notification> page(String email,int page){return notifications.findByUserIdOrderByCreatedAtDescIdDesc(accounts.current(email).getId(),PageRequest.of(Math.max(0,page),20));}
 @Transactional public String open(String email,Long id){Notification n=notifications.findByIdAndUserId(id,accounts.current(email).getId()).orElseThrow(()->new BusinessException("Không tìm thấy thông báo của bạn."));n.setRead(true);return n.getUrl();}
 @Transactional public void readAll(String email){notifications.readAll(accounts.current(email).getId());}
}
