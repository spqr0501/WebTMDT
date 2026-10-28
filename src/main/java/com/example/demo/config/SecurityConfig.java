package com.example.demo.config;
import com.example.demo.repository.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService userDetailsService(UserRepository users){return email -> {
  var u=users.findByEmailIgnoreCase(email.strip()).orElseThrow(()->new UsernameNotFoundException("Sai thông tin đăng nhập"));
  return new AccountPrincipal(u);
 };}
 @Bean SecurityFilterChain security(HttpSecurity http,UserRepository users) throws Exception {
  // Đăng nhập luôn về trang chủ, không ghi nhớ yêu cầu nền như /sw.js.
  HttpSessionRequestCache requestCache=new HttpSessionRequestCache();
  requestCache.setRequestMatcher(request->false);
  http.csrf(c->c.ignoringRequestMatchers("/payments/stripe/webhook")).requestCache(c->c.requestCache(requestCache))
   .authorizeHttpRequests(a->a
   .requestMatchers(org.springframework.http.HttpMethod.POST,"/payments/stripe/webhook").permitAll()
   .requestMatchers("/payments/**").authenticated()
   .requestMatchers("/admin/**").hasRole("ADMIN")
   .requestMatchers("/shipper/**").hasRole("SHIPPER")
   .requestMatchers("/api/maps/**","/api/delivery/**").authenticated()
   .requestMatchers("/checkout/**","/orders/**","/account","/account/**","/reviews/**","/notifications/**","/custom-cakes/**","/after-sales/**","/support/**","/assistant/**","/favorites/**").authenticated()
   .requestMatchers("/","/products/**","/cart/**","/login","/register","/forgot-password","/reset-password","/css/**","/js/**","/images/**","/media/**","/about","/delivery","/care","/contact","/policies","/image-credits","/error").permitAll()
   .anyRequest().denyAll())
   // alwaysUseDefaultTargetUrl cũng xóa yêu cầu cũ còn lưu trong session.
   .formLogin(f->f.loginPage("/login").defaultSuccessUrl("/",true).permitAll())
   .logout(l->l.logoutSuccessUrl("/?logout"))
   .sessionManagement(s->s.sessionFixation(f->f.migrateSession()))
   .addFilterBefore(new ActiveAccountFilter(users),org.springframework.security.web.access.intercept.AuthorizationFilter.class);
  return http.build();
 }
}
