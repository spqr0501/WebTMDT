package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import java.util.*;
@Service
public class FavoriteService {
 private final FavoriteRepository favorites;private final ProductRepository products;private final AccountService accounts;private final UserRepository users;
 public FavoriteService(FavoriteRepository favorites,ProductRepository products,AccountService accounts,UserRepository users){this.favorites=favorites;this.products=products;this.accounts=accounts;this.users=users;}
 public Set<Long> ids(String email){return favorites.productIds(accounts.current(email).getId());}
 public Page<Favorite> page(String email,int page){return favorites.findByUserIdAndProductActiveTrueOrderByCreatedAtDescIdDesc(accounts.current(email).getId(),PageRequest.of(Math.max(0,page),12));}
 @Transactional public void set(String email,Long productId,boolean favorite){
  var user=users.lockById(accounts.current(email).getId()).orElseThrow();var existing=favorites.findByUserIdAndProductId(user.getId(),productId);
  if(!favorite){existing.ifPresent(favorites::delete);return;}
  var product=products.findById(productId).filter(Product::getActive).orElseThrow(()->new BusinessException("Bánh không còn được bán."));
  if(existing.isEmpty()){var f=new Favorite();f.setUser(user);f.setProduct(product);favorites.save(f);}
 }
}
