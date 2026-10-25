package com.example.demo.config;
import com.example.demo.entity.User;
import org.springframework.security.core.authority.AuthorityUtils;
/** Lưu phiên bản xác thực để mật khẩu mới thu hồi các phiên đăng nhập cũ. */
public class AccountPrincipal extends org.springframework.security.core.userdetails.User {
 private final long authVersion;
 public AccountPrincipal(User u){super(u.getEmail(),u.getPassword(),u.getEnabled(),true,true,true,AuthorityUtils.createAuthorityList("ROLE_"+u.getRole().name()));authVersion=u.getAuthVersion();}
 public long getAuthVersion(){return authVersion;}
}
