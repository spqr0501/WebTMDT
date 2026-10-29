package com.example.demo.controller;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
@ControllerAdvice
public class GlobalAdvice {
 @org.springframework.beans.factory.annotation.Autowired private com.example.demo.repository.CustomerOrderRepository refundOrders;
 @org.springframework.beans.factory.annotation.Autowired private com.example.demo.repository.CustomCakeRequestRepository refundCustom;
 @ModelAttribute("pendingRefundCount") public long pendingRefundCount(org.springframework.security.core.Authentication auth){
  if(auth==null||auth.getAuthorities().stream().noneMatch(a->a.getAuthority().equals("ROLE_ADMIN")))return 0;
  return refundOrders.countByStatusAndPaymentPaidGreaterThan(com.example.demo.entity.OrderStatus.CANCELLED,java.math.BigDecimal.ZERO)+refundCustom.countByStatusInAndPaymentPaidGreaterThan(java.util.List.of(com.example.demo.entity.CustomCakeStatus.CANCELLED,com.example.demo.entity.CustomCakeStatus.REJECTED),java.math.BigDecimal.ZERO);
 }

 @org.springframework.beans.factory.annotation.Autowired private com.example.demo.config.StripeSettings stripeSettings;
 @org.springframework.beans.factory.annotation.Autowired private FavoriteService favorites;
 @ModelAttribute("favoriteIds") public java.util.Set<Long> favoriteIds(java.security.Principal p){return p==null?java.util.Set.of():favorites.ids(p.getName());}
 @org.springframework.beans.factory.annotation.Value("${app.shop.phone:}") private String shopPhone;
 @org.springframework.beans.factory.annotation.Value("${app.shop.address:}") private String shopAddress;
 @org.springframework.beans.factory.annotation.Value("${app.shop.hours:}") private String shopHours;
 @org.springframework.beans.factory.annotation.Value("${app.maps.tiles-url:https://tile.openstreetmap.org/{z}/{x}/{y}.png}") private String mapTilesUrl;
 private final NotificationService notices;private final CategoryRepository categories;private final CartService cart;
 public GlobalAdvice(CategoryRepository categories,CartService cart,NotificationService notices){this.notices=notices;this.categories=categories;this.cart=cart;}
 @ModelAttribute public void global(Model model,HttpSession session,java.security.Principal principal){model.addAttribute("stripeReady",stripeSettings.ready());model.addAttribute("mapTilesUrl",mapTilesUrl);model.addAttribute("unreadNotifications",principal==null?0L:notices.unread(principal.getName()));model.addAttribute("shopPhone",shopPhone);model.addAttribute("shopAddress",shopAddress);model.addAttribute("shopHours",shopHours);model.addAttribute("categories",categories.findAll(org.springframework.data.domain.Sort.by("name")));model.addAttribute("cartCount",cart.snapshot(session).values().stream().mapToInt(Integer::intValue).sum());}
 @ExceptionHandler(BusinessException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public String business(BusinessException e,Model model){model.addAttribute("message",e.getMessage());return "error";}
 @ExceptionHandler({org.springframework.dao.DataIntegrityViolationException.class,org.springframework.dao.ConcurrencyFailureException.class}) @ResponseStatus(HttpStatus.CONFLICT)
 public String conflict(Exception e,Model model){model.addAttribute("message","Dữ liệu trùng hoặc vừa được cập nhật. Vui lòng tải lại và thử lại.");return "error";}
 @ExceptionHandler({org.springframework.web.multipart.MaxUploadSizeExceededException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class}) @ResponseStatus(HttpStatus.BAD_REQUEST)
 public String input(Exception e,Model model){model.addAttribute("message","Dữ liệu không hợp lệ hoặc tệp vượt quá 30 MB.");return "error";}
}
