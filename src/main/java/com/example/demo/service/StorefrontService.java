package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;
@Service
public class StorefrontService {
 private final ProductRepository products;private final OrderItemRepository items;private final VoucherRepository vouchers;
 public StorefrontService(ProductRepository products,OrderItemRepository items,VoucherRepository vouchers){this.products=products;this.items=items;this.vouchers=vouchers;}
 public List<Product> popular(){return items.popularIds(PageRequest.of(0,3)).stream().map(id->products.findById(id).orElseThrow()).toList();}
 public List<Voucher> offers(){return vouchers.currentOffers(BakeryClock.now(),PageRequest.of(0,3));}
 public List<Product> latest(){return products.findTop6ByActiveTrueOrderByIdDesc();}
 public List<Product> related(Product p){return products.findTop3ByActiveTrueAndCategoryIdAndIdNotOrderByIdDesc(p.getCategory().getId(),p.getId());}
}
