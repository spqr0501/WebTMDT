package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;
import java.util.function.*;
@Service
public class PersistentCartStore {
 private final UserRepository users;private final ProductRepository products;private final CartEntryRepository entries;private final TransactionTemplate tx;
 public PersistentCartStore(UserRepository users,ProductRepository products,CartEntryRepository entries,PlatformTransactionManager manager){this.users=users;this.products=products;this.entries=entries;this.tx=new TransactionTemplate(manager);}
 public <T>T locked(String email,Supplier<T> work){return tx.execute(status->{var user=users.lockById(userId(email)).orElseThrow();if(!user.getEnabled())throw new BusinessException("Tài khoản đã bị khóa.");return work.get();});}
 private Long userId(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->new BusinessException("Tài khoản không tồn tại.")).getId();}
 public Map<CartService.Key,Integer> load(String email){var result=new TreeMap<CartService.Key,Integer>();for(var e:entries.findByUserId(userId(email)))result.put(new CartService.Key(e.getProduct().getId(),e.getOptionId()),e.getQuantity());return result;}
 public void save(String email,Map<CartService.Key,Integer> values){locked(email,()->{Long id=userId(email);entries.deleteForUser(id);for(var e:values.entrySet()){var line=new CartEntry();line.setUser(users.getReferenceById(id));line.setProduct(products.getReferenceById(e.getKey().productId()));line.setOptionId(e.getKey().optionId());line.setQuantity(e.getValue());entries.save(line);}entries.flush();return null;});}
 public Map<CartService.Key,Integer> merge(String email,Map<CartService.Key,Integer> guest,UnaryOperator<Map<CartService.Key,Integer>> normalize){return locked(email,()->{Map<CartService.Key,Integer> merged=load(email);guest.forEach((key,qty)->merged.merge(key,qty,(a,b)->(int)Math.min(99,(long)a+b)));var normalized=normalize.apply(merged);save(email,normalized);return normalized;});}
}
