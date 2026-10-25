package com.example.demo.config;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;
public class ActiveAccountFilter extends OncePerRequestFilter {
 private final UserRepository users;
 public ActiveAccountFilter(UserRepository users){this.users=users;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
  var auth=SecurityContextHolder.getContext().getAuthentication();
  if(auth!=null&&auth.isAuthenticated()&&!(auth instanceof AnonymousAuthenticationToken)){
   var user=users.findByEmailIgnoreCase(auth.getName());
   if(user.isEmpty()||!user.get().getEnabled()||(auth.getPrincipal() instanceof AccountPrincipal p&&p.getAuthVersion()!=user.get().getAuthVersion())){
    new SecurityContextLogoutHandler().logout(req,res,auth);res.sendRedirect(req.getContextPath()+"/login?error");return;
   }
  }
  chain.doFilter(req,res);
 }
}
