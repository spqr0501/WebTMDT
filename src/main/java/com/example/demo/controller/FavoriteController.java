package com.example.demo.controller;
import com.example.demo.service.FavoriteService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import java.security.Principal;
@Controller
public class FavoriteController {
 private final FavoriteService favorites;
 public FavoriteController(FavoriteService favorites){this.favorites=favorites;}
 @GetMapping("/favorites") public String page(Principal p,@RequestParam(defaultValue="0") int page,Model m){var data=favorites.page(p.getName(),page);m.addAttribute("page",data);m.addAttribute("products",data.getContent().stream().map(com.example.demo.entity.Favorite::getProduct).toList());m.addAttribute("favoritePage",true);return "account/favorites";}
 @PostMapping("/favorites/{id}/state") @ResponseBody public org.springframework.http.ResponseEntity<java.util.Map<String,Object>> state(@PathVariable Long id,@RequestParam boolean favorite,Principal p){favorites.set(p.getName(),id,favorite);return org.springframework.http.ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore()).body(java.util.Map.of("favorite",favorite,"message",favorite?"Đã lưu bánh yêu thích.":"Đã bỏ bánh khỏi yêu thích."));}
 @PostMapping("/favorites/{id}") public String set(@PathVariable Long id,@RequestParam boolean favorite,@RequestParam(defaultValue="product") String back,Principal p,RedirectAttributes flash){favorites.set(p.getName(),id,favorite);flash.addFlashAttribute("success",favorite?"Đã thêm bánh vào danh sách yêu thích.":"Đã bỏ bánh khỏi danh sách yêu thích.");return back.equals("favorites")?"redirect:/favorites":"redirect:/products/"+id;}
}
