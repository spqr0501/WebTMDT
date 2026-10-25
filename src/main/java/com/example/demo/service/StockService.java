package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service
public class StockService {
 private final StockMovementRepository movements;
 private final NotificationService notices;
 public StockService(StockMovementRepository movements,NotificationService notices){this.movements=movements;this.notices=notices;}
 @Transactional(propagation=Propagation.MANDATORY)
 public void record(Product product,int delta,String reason,String actor){
  StockMovement m=new StockMovement();m.setProduct(product);m.setDelta(delta);m.setBalance(product.getStock());m.setReason(reason);m.setActor(actor);movements.save(m);
  int balance=product.getStock(),previous=balance-delta;
  if(product.getActive()&&((balance==0&&previous>0)||(balance>0&&balance<5&&(previous>=5||previous==0)))){
   notices.admins(balance==0?"Bánh đã hết hàng":"Bánh sắp hết hàng",product.getName()+" (#"+product.getId()+") còn "+balance+" bánh. "+reason+". Bấm để xem danh sách và bổ sung tồn kho.","/admin/products?stock="+(balance==0?"out":"low"));
  }
 }
}
