package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.dto.OptionForm;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
@Service
public class OptionService {
 public record Selection(String label,BigDecimal price){}
 private final ProductOptionRepository options;private final ProductRepository products;
 public OptionService(ProductOptionRepository options,ProductRepository products){this.options=options;this.products=products;}
 public Selection select(Product product,Long optionId){
  if(optionId==null||optionId==0){
   if(options.existsByProductIdAndActiveTrue(product.getId()))throw new BusinessException("Vui lòng chọn kích thước và hương vị cho "+product.getName()+".");
   return new Selection("Tiêu chuẩn",product.getPrice());
  }
  ProductOption o=options.findByIdAndProductIdAndActiveTrue(optionId,product.getId()).orElseThrow(()->new BusinessException("Lựa chọn bánh không còn bán. Vui lòng cập nhật giỏ hàng."));
  return new Selection(o.getLabel(),product.getPrice().add(o.getExtraPrice()));
 }
 @Transactional public void add(Long productId,OptionForm f){
  Product p=products.lockById(productId).orElseThrow();
  if(f.getSizeLabel()==null||f.getSizeLabel().isBlank()||f.getFlavor()==null||f.getFlavor().isBlank()||f.getExtraPrice()==null||f.getExtraPrice().signum()<0)throw new BusinessException("Thông tin lựa chọn bánh không hợp lệ.");
  if(options.findByProductIdOrderByIdAsc(productId).size()>=30)throw new BusinessException("Tối đa 30 lựa chọn mỗi sản phẩm.");
  ProductOption o=new ProductOption();o.setProduct(p);o.setSizeLabel(f.getSizeLabel().strip());o.setFlavor(f.getFlavor().strip());o.setExtraPrice(f.getExtraPrice());options.save(o);
 }
 @Transactional public void disable(Long productId,Long id){
  products.lockById(productId).orElseThrow();
  ProductOption o=options.findByIdAndProductIdAndActiveTrue(id,productId).orElseThrow(()->new BusinessException("Không tìm thấy lựa chọn."));o.setActive(false);
 }
}
