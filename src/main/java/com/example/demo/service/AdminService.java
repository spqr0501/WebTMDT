package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.dto.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.Locale;
@Service
public class AdminService {
 private final ProductRepository products;private final CategoryRepository categories;private final VoucherRepository vouchers;private final MediaService media;private final StockService stockLog;
 public AdminService(ProductRepository products,CategoryRepository categories,VoucherRepository vouchers,MediaService media,StockService stockLog){this.stockLog=stockLog;this.products=products;this.categories=categories;this.vouchers=vouchers;this.media=media;}
 @Transactional public void product(ProductForm f,MultipartFile image,MultipartFile video){product(f,image,video,"admin");}
 @Transactional public void product(ProductForm f,MultipartFile image,MultipartFile video,String actor){
  Product p=f.getId()==null?new Product():products.lockById(f.getId()).orElseThrow(()->new BusinessException("Không tìm thấy sản phẩm."));
  p.setIngredients(f.getIngredients());p.setAllergens(f.getAllergens());p.setStorageInstructions(f.getStorageInstructions());p.setName(f.getName());p.setDescription(f.getDescription());p.setPrice(f.getPrice());
  // Existing inventory changes go through the adjustment endpoint to avoid stale form overwrites.
  if(f.getId()==null)p.setStock(f.getStock());
  p.setCategory(categories.findById(f.getCategoryId()).orElseThrow(()->new BusinessException("Danh mục không hợp lệ.")));p.setActive(f.getActive());p.setFeatured(f.getFeatured());
  String img=media.save(image,false);String vid=media.save(video,true);if(img!=null)p.setImageUrl(img);if(vid!=null)p.setVideoUrl(vid);products.save(p);if(f.getId()==null&&p.getStock()>0)stockLog.record(p,p.getStock(),"Tồn kho ban đầu",actor);
 }
 @Transactional public void gallery(Long id,java.util.List<MultipartFile> files){Product p=products.lockById(id).orElseThrow();var uploads=files.stream().filter(f->!f.isEmpty()).toList();if(p.getGallery().size()+uploads.size()>6)throw new BusinessException("Tối đa 6 ảnh bổ sung.");for(var f:uploads)p.getGallery().add(media.save(f,false));}
 @Transactional public void removeGallery(Long id,int index){Product p=products.lockById(id).orElseThrow();if(index<0||index>=p.getGallery().size())throw new BusinessException("Ảnh không tồn tại.");p.getGallery().remove(index);}
 @Transactional public void archiveProduct(Long id){Product p=products.lockById(id).orElseThrow();p.setActive(false);}
 @Transactional public void stock(Long id,int delta){stock(id,delta,"Điều chỉnh thủ công","admin");}
 @Transactional public void stock(Long id,int delta,String reason,String actor){
 if(reason==null||reason.isBlank()||reason.length()>300)throw new BusinessException("Nhập lý do điều chỉnh, tối đa 300 ký tự.");
  if(delta==0||delta < -1000000||delta>1000000)throw new BusinessException("Mức điều chỉnh tồn kho không hợp lệ.");
  Product p=products.lockById(id).orElseThrow();long next=(long)p.getStock()+delta;
  if(next<0||next>1000000)throw new BusinessException("Tồn kho sau điều chỉnh phải từ 0 đến 1.000.000.");p.setStock((int)next);stockLog.record(p,delta,reason,actor);
 }
 @Transactional public void category(CategoryForm f){
  Category c=f.getId()==null?new Category():categories.findById(f.getId()).orElseThrow();c.setName(f.getName().strip());c.setDescription(f.getDescription());categories.saveAndFlush(c);
 }
 @Transactional public void deleteCategory(Long id){if(products.existsByCategoryId(id))throw new BusinessException("Danh mục đang có sản phẩm. Hãy chuyển sản phẩm sang danh mục khác trước.");categories.deleteById(id);}
 @Transactional public void voucher(VoucherForm f){
  if(!f.getEndsAt().isAfter(f.getStartsAt()))throw new BusinessException("Thời điểm kết thúc phải sau thời điểm bắt đầu.");
  if(f.getType()==DiscountType.PERCENT&&f.getAmount().compareTo(BigDecimal.valueOf(100))>0)throw new BusinessException("Mức giảm phần trăm không vượt quá 100%.");
  Voucher v=f.getId()==null?new Voucher():vouchers.lockById(f.getId()).orElseThrow();
  String code=f.getCode().strip().toUpperCase(Locale.ROOT);
  if(v.getUsedCount()>0&&!code.equals(v.getCode()))throw new BusinessException("Không đổi mã đã có lượt sử dụng.");
  if(f.getUsageLimit()<v.getUsedCount())throw new BusinessException("Giới hạn không được thấp hơn số lượt đã dùng.");
  v.setCode(code);v.setType(f.getType());v.setAmount(f.getAmount());v.setMinOrder(f.getMinOrder());v.setMaxDiscount(f.getMaxDiscount());v.setStartsAt(f.getStartsAt());v.setEndsAt(f.getEndsAt());v.setUsageLimit(f.getUsageLimit());v.setPerUserLimit(f.getPerUserLimit());v.setActive(f.getActive());vouchers.saveAndFlush(v);
 }
 @Transactional public void deleteVoucher(Long id){Voucher v=vouchers.lockById(id).orElseThrow();if(v.getUsedCount()>0)v.setActive(false);else vouchers.delete(v);}
}
