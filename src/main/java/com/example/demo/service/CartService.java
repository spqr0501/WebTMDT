package com.example.demo.service;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import com.example.demo.entity.Product;
import java.util.*;
import java.math.BigDecimal;
@Service
public class CartService {
 public record Key(Long productId,Long optionId) implements java.io.Serializable,Comparable<Key>{
  public Key { if(optionId==null)optionId=0L; }
  public int compareTo(Key other){int c=productId.compareTo(other.productId);return c==0?optionId.compareTo(other.optionId):c;}
 }
 public record Line(Product product,Long optionId,String optionLabel,BigDecimal unitPrice,int quantity,boolean available){public BigDecimal getTotal(){return unitPrice.multiply(BigDecimal.valueOf(quantity));}}
 private final com.example.demo.repository.ProductOptionRepository optionRepository;private final CatalogService catalog;private final OptionService options;private final com.example.demo.repository.ProductRepository products;
 public CartService(CatalogService catalog,OptionService options,com.example.demo.repository.ProductRepository products,com.example.demo.repository.ProductOptionRepository optionRepository){this.optionRepository=optionRepository;this.catalog=catalog;this.options=options;this.products=products;}
 @SuppressWarnings("unchecked") public Map<Key,Integer> detailed(HttpSession session){synchronized(session){
  var cart=(Map<Key,Integer>)session.getAttribute("cakeCart");
  if(cart!=null)return new TreeMap<>(cart);
  Map<Key,Integer> migrated=new TreeMap<>();var old=(Map<Long,Integer>)session.getAttribute("cart");
  if(old!=null){old.forEach((id,qty)->migrated.put(new Key(id,0L),qty));session.removeAttribute("cart");session.setAttribute("cakeCart",migrated);}
  return migrated;
 }}
 public Map<Long,Integer> snapshot(HttpSession s){Map<Long,Integer> result=new TreeMap<>();detailed(s).forEach((k,v)->result.merge(k.productId(),v,Integer::sum));return result;}
 public void update(HttpSession s,Long id,int quantity,boolean add){update(s,id,0L,quantity,add);}
 public void update(HttpSession session,Long id,Long optionId,int quantity,boolean add){synchronized(session){
  Map<Key,Integer> cart=detailed(session);Key key=new Key(id,optionId);
  if(quantity<0||quantity>99)throw new BusinessException("Số lượng phải từ 0 đến 99.");
  int next=quantity+(add?cart.getOrDefault(key,0):0);
  if(next==0){cart.remove(key);}else{
   Product p=catalog.product(id);options.select(p,optionId);
   int others=cart.entrySet().stream().filter(e->e.getKey().productId().equals(id)&&!e.getKey().equals(key)).mapToInt(Map.Entry::getValue).sum();
   if(next>99||next+others>p.getStock())throw new BusinessException("Số lượng vượt quá tồn kho hoặc giới hạn 99 bánh.");
   if(!cart.containsKey(key)&&cart.size()>=50)throw new BusinessException("Giỏ hàng tối đa 50 lựa chọn bánh.");
   cart.put(key,next);
  }
  session.setAttribute("cakeCart",cart);
 }}
 public List<Line> lines(HttpSession s){return detailed(s).entrySet().stream().map(e->{
  Product p=products.findById(e.getKey().productId()).orElseThrow(()->new BusinessException("Sản phẩm đã bị xóa. Vui lòng làm trống giỏ hàng."));
  try{var choice=options.select(p,e.getKey().optionId());return new Line(p,e.getKey().optionId(),choice.label(),choice.price(),e.getValue(),p.getActive()&&p.getStock()>=e.getValue());}
  catch(BusinessException ex){return new Line(p,e.getKey().optionId(),"Lựa chọn đã ngừng bán — vui lòng xóa khỏi giỏ",BigDecimal.ZERO,e.getValue(),false);}
 }).toList();}
 public BigDecimal subtotal(HttpSession s){return lines(s).stream().map(Line::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);}
 public String reorder(com.example.demo.entity.CustomerOrder order,HttpSession session){synchronized(session){
  Map<Key,Integer> next=detailed(session);List<String> skipped=new ArrayList<>();int added=0;
  for(var item:order.getItems()){
   try{
    Product p=catalog.product(item.getProduct().getId());Long optionId=item.getOptionId();
    if(optionId==null){var choices=optionRepository.findByProductIdAndActiveTrueOrderByExtraPriceAsc(p.getId());if(choices.isEmpty())optionId=0L;else optionId=choices.stream().filter(o->o.getLabel().equals(item.getOptionLabel())).map(o->o.getId()).findFirst().orElseThrow(()->new BusinessException("Lựa chọn cũ không còn bán."));}
    options.select(p,optionId);Key key=new Key(p.getId(),optionId);
    int used=next.entrySet().stream().filter(e->e.getKey().productId().equals(p.getId())).mapToInt(Map.Entry::getValue).sum();
    int quantity=Math.min(item.getQuantity(),Math.min(p.getStock()-used,99-next.getOrDefault(key,0)));
    if(quantity<=0||(!next.containsKey(key)&&next.size()>=50))throw new BusinessException("Không đủ tồn kho hoặc giỏ hàng đã đầy.");
    next.merge(key,quantity,Integer::sum);added+=quantity;if(quantity<item.getQuantity())skipped.add(p.getName()+": chỉ thêm được "+quantity);
   }catch(BusinessException e){skipped.add(item.getProductName()+": "+e.getMessage());}
  }
  session.setAttribute("cakeCart",next);session.removeAttribute("checkoutToken");
  return "Đã thêm "+added+" bánh vào giỏ. Giá và tồn kho được tính theo hiện tại; hãy chọn lại ngày nhận và kiểm tra phụ kiện."+(skipped.isEmpty()?"":" Không thêm đủ: "+String.join("; ",skipped));
 }}
 public void clear(HttpSession s){s.removeAttribute("cakeCart");s.removeAttribute("cart");s.removeAttribute("checkoutToken");s.removeAttribute("reorderAccessories");}
}
