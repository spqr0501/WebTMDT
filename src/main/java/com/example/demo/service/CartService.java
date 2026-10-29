package com.example.demo.service;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import com.example.demo.entity.Product;
import java.util.*;
import java.math.BigDecimal;
@Service
public class CartService {
 @org.springframework.beans.factory.annotation.Autowired private PersistentCartStore storage;
 private String email(){var a=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();return a!=null&&a.isAuthenticated()&&!(a instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)?a.getName().toLowerCase(java.util.Locale.ROOT):null;}
 private <T>T locked(HttpSession session,java.util.function.Supplier<T> work){synchronized(session){String account=email();if(account==null)return work.get();detailed(session);return storage.locked(account,work);}}
 private void persist(HttpSession session,Map<Key,Integer> values){session.setAttribute("cakeCart",new TreeMap<>(values));String account=email();if(account!=null)storage.save(account,values);}
 private Map<Key,Integer> normalizeMerge(HttpSession session,Map<Key,Integer> values){Map<Key,Integer> result=new TreeMap<>();Map<Long,Integer> used=new HashMap<>();boolean changed=false;for(var e:new TreeMap<>(values).entrySet()){try{var p=catalog.product(e.getKey().productId());options.select(p,e.getKey().optionId());int qty=Math.min(Math.min(99,e.getValue()),Math.max(0,p.getStock()-used.getOrDefault(p.getId(),0)));if(qty!=e.getValue()||result.size()>=50)changed=true;if(qty>0&&result.size()<50){result.put(e.getKey(),qty);used.merge(p.getId(),qty,Integer::sum);}}catch(BusinessException ex){changed=true;}}if(changed)warn(session,"Giỏ đã được đồng bộ theo giá và tồn hiện tại; bánh hết hàng/ngừng bán hoặc vượt số lượng được điều chỉnh. Kiểm tra giỏ trước khi đặt.");return result;}
 public void mergeOnLogin(HttpSession session,String account){synchronized(session){detailed(session);}}

 public record Key(Long productId,Long optionId) implements java.io.Serializable,Comparable<Key>{
  public Key { if(optionId==null)optionId=0L; }
  public int compareTo(Key other){int c=productId.compareTo(other.productId);return c==0?optionId.compareTo(other.optionId):c;}
 }
 public record Line(Product product,Long optionId,String optionLabel,BigDecimal unitPrice,int quantity,boolean available,int maxAllowed){public BigDecimal getTotal(){return unitPrice.multiply(BigDecimal.valueOf(quantity));}}
 private final com.example.demo.repository.ProductOptionRepository optionRepository;private final CatalogService catalog;private final OptionService options;private final com.example.demo.repository.ProductRepository products;
 public CartService(CatalogService catalog,OptionService options,com.example.demo.repository.ProductRepository products,com.example.demo.repository.ProductOptionRepository optionRepository){this.optionRepository=optionRepository;this.catalog=catalog;this.options=options;this.products=products;}
 @SuppressWarnings("unchecked") public Map<Key,Integer> detailed(HttpSession session){synchronized(session){
  String account=email(),owner=(String)session.getAttribute("cartOwner");
  var existing=(Map<Key,Integer>)session.getAttribute("cakeCart");
  Map<Key,Integer> guest=new TreeMap<>();if(owner==null&&existing!=null)guest.putAll(existing);
  var old=(Map<Long,Integer>)session.getAttribute("cart");if(owner==null&&old!=null)old.forEach((id,qty)->guest.merge(new Key(id,0L),qty,Integer::sum));
  session.removeAttribute("cart");
  if(account!=null){if(!account.equals(owner)){Map<Key,Integer> merged=storage.merge(account,guest,m->normalizeMerge(session,m));session.setAttribute("cartOwner",account);session.setAttribute("cakeCart",new TreeMap<>(merged));if(!guest.isEmpty())warn(session,"Đã gộp giỏ khách vào giỏ tài khoản. Giỏ được lưu để dùng trên thiết bị khác.");return merged;}return storage.load(account);}
  if(owner!=null){session.removeAttribute("cartOwner");session.removeAttribute("cakeCart");return new TreeMap<>();}
  session.setAttribute("cakeCart",new TreeMap<>(guest));return guest;
 }}
 public Map<Long,Integer> snapshot(HttpSession s){Map<Long,Integer> result=new TreeMap<>();detailed(s).forEach((k,v)->result.merge(k.productId(),v,Integer::sum));return result;}
 public void update(HttpSession s,Long id,int quantity,boolean add){update(s,id,0L,quantity,add);}
 public void update(HttpSession session,Long id,Long optionId,int quantity,boolean add){locked(session,()->{updateLocal(session,id,optionId,quantity,add);return null;});}
 private void updateLocal(HttpSession session,Long id,Long optionId,int quantity,boolean add){synchronized(session){
  if(quantity<0)throw new BusinessException("Số lượng không được âm.");
  reconcile(session);Map<Key,Integer> cart=detailed(session);Key key=new Key(id,optionId);
  if(quantity==0&&!add){cart.remove(key);}else{
   Product p=catalog.product(id);options.select(p,optionId);
   int others=cart.entrySet().stream().filter(e->e.getKey().productId().equals(id)&&!e.getKey().equals(key)).mapToInt(Map.Entry::getValue).sum();
   long desired=(long)quantity+(add?cart.getOrDefault(key,0):0);int limit=Math.min(99,Math.max(0,p.getStock()-others));int next=(int)Math.min(desired,limit);
   if(desired>limit)warn(session,p.getName()+": số lượng vượt mức có thể mua. Đã giữ ở "+next+" bánh cho lựa chọn này (tồn hiện tại "+p.getStock()+", các lựa chọn khác trong giỏ "+others+").");
   if(next==0)cart.remove(key);else{if(!cart.containsKey(key)&&cart.size()>=50)throw new BusinessException("Giỏ hàng tối đa 50 lựa chọn bánh.");cart.put(key,next);}
  }
  persist(session,cart);
 }}
 public void reconcile(HttpSession session){locked(session,()->{reconcileLocal(session);return null;});}
 private void reconcileLocal(HttpSession session){synchronized(session){
  Map<Key,Integer> cart=detailed(session);Map<Long,Integer> used=new HashMap<>();boolean changed=false;var changedNames=new LinkedHashSet<String>();
  for(var it=cart.entrySet().iterator();it.hasNext();){var e=it.next();var found=products.findById(e.getKey().productId());if(found.isEmpty()||!found.get().getActive()){changed=true;changedNames.add(found.isPresent()?found.get().getName():"Bánh #"+e.getKey().productId());it.remove();continue;}Product p=found.get();
   int limit=Math.min(99,Math.max(0,p.getStock()-used.getOrDefault(p.getId(),0))),next=Math.min(e.getValue(),limit);
   if(next!=e.getValue()){changed=true;changedNames.add(p.getName());if(next==0)it.remove();else e.setValue(next);}used.merge(p.getId(),next,Integer::sum);
  }
  if(changed){persist(session,cart);warn(session,"Tồn kho vừa thay đổi. Đã giảm số lượng trong giỏ về mức còn bán; bánh hết hàng được bỏ khỏi giỏ: "+String.join(", ",changedNames)+". Vui lòng kiểm tra lại tổng tiền.");}
 }}
 private void warn(HttpSession session,String text){String prior=(String)session.getAttribute("stockWarning");session.setAttribute("stockWarning",prior==null?text:(prior+" "+text).substring(0,Math.min(3000,prior.length()+1+text.length())));}
 public String takeWarning(HttpSession session){synchronized(session){String text=(String)session.getAttribute("stockWarning");session.removeAttribute("stockWarning");return text;}}
 public List<Line> lines(HttpSession s){var current=detailed(s);Map<Long,Integer> totals=new HashMap<>();current.forEach((k,v)->totals.merge(k.productId(),v,Integer::sum));return current.entrySet().stream().map(e->{
  Product p=products.findById(e.getKey().productId()).orElseThrow(()->new BusinessException("Sản phẩm đã bị xóa. Vui lòng làm trống giỏ hàng."));
  try{var choice=options.select(p,e.getKey().optionId());return new Line(p,e.getKey().optionId(),choice.label(),choice.price(),e.getValue(),p.getActive()&&p.getStock()>=totals.get(p.getId()),Math.min(99,Math.max(0,p.getStock()-(totals.get(p.getId())-e.getValue()))));}
  catch(BusinessException ex){return new Line(p,e.getKey().optionId(),"Lựa chọn đã ngừng bán — vui lòng xóa khỏi giỏ",BigDecimal.ZERO,e.getValue(),false,0);}
 }).toList();}
 public BigDecimal subtotal(HttpSession s){return lines(s).stream().map(Line::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);}
 public String reorder(com.example.demo.entity.CustomerOrder order,HttpSession session){return locked(session,()->reorderLocal(order,session));}
 private String reorderLocal(com.example.demo.entity.CustomerOrder order,HttpSession session){synchronized(session){
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
  persist(session,next);session.removeAttribute("checkoutToken");
  return "Đã thêm "+added+" bánh vào giỏ. Giá và tồn kho được tính theo hiện tại; hãy chọn lại ngày nhận và kiểm tra phụ kiện."+(skipped.isEmpty()?"":" Không thêm đủ: "+String.join("; ",skipped));
 }}
 public void removePurchased(HttpSession session,Map<Key,Integer> purchased){locked(session,()->{var current=detailed(session);purchased.forEach((key,qty)->{int left=current.getOrDefault(key,0)-qty;if(left<=0)current.remove(key);else current.put(key,left);});persist(session,current);resetCheckout(session);return null;});}
 private void resetCheckout(HttpSession s){s.removeAttribute("stockWarning");s.removeAttribute("checkoutToken");s.removeAttribute("reorderAccessories");s.removeAttribute("cart");}
 public void clear(HttpSession session){locked(session,()->{persist(session,new TreeMap<>());resetCheckout(session);return null;});}
}
