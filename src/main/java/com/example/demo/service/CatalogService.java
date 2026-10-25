package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
@Service
public class CatalogService {
 private final ProductRepository products;
 public CatalogService(ProductRepository products){this.products=products;}
 public Product product(Long id){return products.findById(id).filter(Product::getActive).orElseThrow(()->new BusinessException("Sản phẩm không còn được bán."));}
 public java.util.List<com.example.demo.dto.SearchSuggestion> suggestions(String query){
  String term=query==null?"":query.strip();
  if(term.isEmpty())return java.util.List.of();
  if(term.length()>100)throw new BusinessException("Từ khóa tối đa 100 ký tự.");
  Specification<Product> spec=(r,c,b)->b.isTrue(r.get("active"));
  spec=spec.and(nameContains(term));
  return products.findAll(spec,PageRequest.of(0,6,Sort.by("name").ascending().and(Sort.by("id").ascending())))
   .stream().map(p->new com.example.demo.dto.SearchSuggestion(p.getId(),p.getName(),p.getPrice(),p.getImageUrl(),shortDescription(p.getDescription()),p.getStock()>0)).toList();
 }
 private static String shortDescription(String text){
  if(text==null)return "";
  String plain=text.replaceAll("\\s+"," ").strip();
  return plain.length()>110?plain.substring(0,110).stripTrailing()+"…":plain;
 }
 private static Specification<Product> nameContains(String query){
  String term=query.strip().toLowerCase(java.util.Locale.ROOT).replace("\\","\\\\").replace("%","\\%").replace("_","\\_");
  return (r,c,b)->b.like(b.lower(r.get("name")),"%"+term+"%",'\\');
 }
 public Page<Product> search(String q,Long category,BigDecimal min,BigDecimal max,int page,String sort){
  if(min!=null&&max!=null&&min.compareTo(max)>0)throw new BusinessException("Giá tối thiểu không được lớn hơn giá tối đa.");
  Specification<Product> spec=(r,c,b)->b.isTrue(r.get("active"));
  if(q!=null&&!q.isBlank()){
   spec=spec.and(nameContains(q));
  }
  if(category!=null)spec=spec.and((r,c,b)->b.equal(r.get("category").get("id"),category));
  if(min!=null)spec=spec.and((r,c,b)->b.greaterThanOrEqualTo(r.get("price"),min));
  if(max!=null)spec=spec.and((r,c,b)->b.lessThanOrEqualTo(r.get("price"),max));
  Sort order="priceAsc".equals(sort)?Sort.by("price").ascending():"priceDesc".equals(sort)?Sort.by("price").descending():Sort.by("id").descending();
  return products.findAll(spec,PageRequest.of(Math.max(page,0),12,order));
 }
}
