package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.dto.AccessoryForm;
import com.example.demo.repository.AccessoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

@Service
public class AccessoryService {
 private final AccessoryRepository accessories;
 public AccessoryService(AccessoryRepository accessories){this.accessories=accessories;}
 public record Choice(Accessory accessory,int quantity){public BigDecimal getTotal(){return accessory.getPrice().multiply(BigDecimal.valueOf(quantity));}}
 public List<Accessory> available(){return accessories.findByActiveTrueOrderByIdAsc();}
 public List<Choice> select(Map<Long,Integer> quantities,boolean lock){
  if(quantities==null||quantities.isEmpty())return List.of();
  if(quantities.size()>10)throw new BusinessException("Tối đa 10 loại phụ kiện.");
  List<Choice> result=new ArrayList<>();
  for(var entry:new TreeMap<>(quantities).entrySet()){
   Integer qty=entry.getValue();if(qty==null||qty<0||qty>20)throw new BusinessException("Mỗi loại phụ kiện chọn từ 0 đến 20.");if(qty==0)continue;
   Accessory a=(lock?accessories.lockById(entry.getKey()):accessories.findById(entry.getKey())).orElseThrow(()->new BusinessException("Phụ kiện không tồn tại."));
   if(!a.getActive()||a.getStock()<qty)throw new BusinessException("Không đủ phụ kiện: "+a.getName());result.add(new Choice(a,qty));
  }
  return result;
 }
 public BigDecimal total(Map<Long,Integer> quantities){return select(quantities,false).stream().map(Choice::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);}
 @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
 public BigDecimal attach(CustomerOrder order,Map<Long,Integer> quantities){
  BigDecimal total=BigDecimal.ZERO;
  for(Choice c:select(quantities,true)){Accessory a=c.accessory();OrderAccessory line=new OrderAccessory();line.setOrder(order);line.setAccessory(a);line.setName(a.getName());line.setUnitPrice(a.getPrice());line.setQuantity(c.quantity());order.getAccessories().add(line);a.setStock(a.getStock()-c.quantity());total=total.add(c.getTotal());}
  return total;
 }
 @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
 public void restore(CustomerOrder order){order.getAccessories().stream().sorted(Comparator.comparing(l->l.getAccessory().getId())).forEach(line->{Accessory a=accessories.lockById(line.getAccessory().getId()).orElseThrow();a.setStock(Math.addExact(a.getStock(),line.getQuantity()));});}
 @Transactional public void save(AccessoryForm form){
  Accessory a=form.getId()==null?new Accessory():accessories.lockById(form.getId()).orElseThrow(()->new BusinessException("Không tìm thấy phụ kiện."));
  if(form.getActive()&&(form.getId()==null||!a.getActive())&&accessories.countByActiveTrue()>=10)throw new BusinessException("Tối đa 10 loại phụ kiện đang bán. Hãy ngừng bán một loại trước khi thêm.");
  a.setName(form.getName().strip());a.setPrice(form.getPrice());a.setActive(form.getActive());
  if(form.getId()==null)a.setStock(form.getStock());accessories.save(a);
 }
 @Transactional public void stock(Long id,int delta){Accessory a=accessories.lockById(id).orElseThrow();long next=(long)a.getStock()+delta;if(delta==0||next<0||next>1000000)throw new BusinessException("Tồn kho phụ kiện phải từ 0 đến 1.000.000.");a.setStock((int)next);}
 @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
 @Transactional public boolean delete(Long id){var a=accessories.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy phụ kiện."));long count=entityManager.createQuery("select count(l) from OrderAccessory l where l.accessory.id=:id",Long.class).setParameter("id",id).getSingleResult();if(count>0){a.setActive(false);return false;}accessories.delete(a);return true;}
}
