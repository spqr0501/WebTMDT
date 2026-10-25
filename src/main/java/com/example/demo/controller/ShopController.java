package com.example.demo.controller;
import com.example.demo.service.*;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
@Controller
public class ShopController {
 private final ImageCreditService imageCredits;
 private final StorefrontService storefront;private final ReviewService reviewService;private final com.example.demo.repository.ReviewRepository reviews;private final com.example.demo.repository.ProductOptionRepository options;private final CatalogService catalog;private final CartService cart;private final ProductRepository products;
 public ShopController(ImageCreditService imageCredits,CatalogService catalog,CartService cart,ProductRepository products,StorefrontService storefront,ReviewService reviewService,com.example.demo.repository.ReviewRepository reviews,com.example.demo.repository.ProductOptionRepository options){this.imageCredits=imageCredits;this.storefront=storefront;this.reviewService=reviewService;this.reviews=reviews;this.options=options;this.catalog=catalog;this.cart=cart;this.products=products;}
 @GetMapping("/") public String home(Model m){m.addAttribute("products",products.findTop6ByActiveTrueAndFeaturedTrueOrderByIdDesc());m.addAttribute("latest",storefront.latest());m.addAttribute("popular",storefront.popular());m.addAttribute("offers",storefront.offers());return "shop/home";}
 @GetMapping(value="/products/suggestions",produces="application/json") @ResponseBody
 public java.util.List<com.example.demo.dto.SearchSuggestion> suggestions(@RequestParam(defaultValue="") String q){return catalog.suggestions(q);}
 @GetMapping("/products") public String products(@RequestParam(defaultValue="") String q,@RequestParam(required=false) Long category,@RequestParam(required=false) BigDecimal min,@RequestParam(required=false) BigDecimal max,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="newest") String sort,Model m){m.addAttribute("page",catalog.search(q,category,min,max,page,sort));m.addAttribute("q",q);m.addAttribute("category",category);m.addAttribute("min",min);m.addAttribute("max",max);m.addAttribute("sort",sort);return "shop/products";}
 @GetMapping("/products/{id}") public String detail(@PathVariable Long id,@RequestParam(defaultValue="0") int reviewPage,@RequestParam(required=false) Integer stars,java.security.Principal principal,Model m){
  if(stars!=null&&(stars<1||stars>5))throw new BusinessException("Bộ lọc sao không hợp lệ.");
  var product=catalog.product(id);m.addAttribute("product",product);m.addAttribute("imageCredit",imageCredits.forImage(product.getImageUrl()));m.addAttribute("options",options.findByProductIdAndActiveTrueOrderByExtraPriceAsc(id));m.addAttribute("related",storefront.related(product));
  m.addAttribute("rating",reviewService.summary(id));m.addAttribute("reviews",reviews.published(id,stars,org.springframework.data.domain.PageRequest.of(Math.max(0,reviewPage),5)));m.addAttribute("stars",stars);
  m.addAttribute("canReview",reviewService.canReview(id,principal==null?null:principal.getName()));if(!m.containsAttribute("reviewForm"))m.addAttribute("reviewForm",new com.example.demo.dto.ReviewForm());return "shop/detail";
 }
 @GetMapping("/cart") public String cart(HttpSession session,Model m){m.addAttribute("unavailable",cart.lines(session).stream().anyMatch(l->!l.available()));m.addAttribute("lines",cart.lines(session));m.addAttribute("subtotal",cart.subtotal(session));return "shop/cart";}
 @PostMapping("/cart/add") public String add(@RequestParam Long id,@RequestParam(defaultValue="1") int quantity,@RequestParam(defaultValue="0") Long optionId,HttpSession session,RedirectAttributes flash){cart.update(session,id,optionId,quantity,true);flash.addFlashAttribute("success","Đã thêm bánh vào giỏ hàng.");return "redirect:/cart";}
 @PostMapping("/cart/update") public String update(@RequestParam Long id,@RequestParam int quantity,@RequestParam(defaultValue="0") Long optionId,HttpSession session){cart.update(session,id,optionId,quantity,false);return "redirect:/cart";}
 @PostMapping("/cart/remove") public String remove(@RequestParam Long id,@RequestParam(defaultValue="0") Long optionId,HttpSession session){cart.update(session,id,optionId,0,false);return "redirect:/cart";}
 @PostMapping("/cart/clear") public String clear(HttpSession session){cart.clear(session);return "redirect:/cart";}
 @GetMapping({"/about","/delivery","/care","/contact"}) public String info(jakarta.servlet.http.HttpServletRequest request,Model model){model.addAttribute("section",request.getRequestURI().substring(request.getContextPath().length()));return "shop/info";}
}
