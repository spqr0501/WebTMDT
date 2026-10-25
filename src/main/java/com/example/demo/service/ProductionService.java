package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class ProductionService {
 public record Job(LocalDate date,String slot,String code,String recipient,String summary,String message,String status,String fulfillment,String payment,String url,boolean tentative,boolean overdue){}
 public record WorkDay(LocalDate date,List<Job> jobs,Map<String,Integer> cakes,long customJobs){}
 public record Schedule(LocalDate from,LocalDate to,String mode,List<WorkDay> days,int jobs,int cakes,long customJobs){}
 private final CustomerOrderRepository orders;private final CustomCakeRequestRepository custom;
 public ProductionService(CustomerOrderRepository orders,CustomCakeRequestRepository custom){this.orders=orders;this.custom=custom;}
 @Transactional(readOnly=true) public Schedule schedule(LocalDate from,LocalDate to,String mode){
  LocalDate start=from==null?LocalDate.now():from,end=to==null?start.plusDays(6):to;
  if(end.isBefore(start)||java.time.temporal.ChronoUnit.DAYS.between(start,end)>30)throw new BusinessException("Chọn khoảng lịch từ 1 đến 31 ngày.");
  if(!Set.of("active","all").contains(mode))throw new BusinessException("Bộ lọc lịch không hợp lệ.");
  Map<LocalDate,List<Job>> jobs=new TreeMap<>();Map<LocalDate,Map<String,Integer>> cakes=new TreeMap<>();
  for(LocalDate d=start;!d.isAfter(end);d=d.plusDays(1)){jobs.put(d,new ArrayList<>());cakes.put(d,new TreeMap<>());}
  for(var o:orders.findByDeliveryDateBetweenOrderByDeliveryDateAscDeliverySlotAsc(start,end)){
   if(o.getStatus()==OrderStatus.CANCELLED||("active".equals(mode)&&o.getStatus()==OrderStatus.COMPLETED))continue;
   String summary=o.getItems().stream().map(i->i.getProductName()+" · "+Objects.toString(i.getOptionLabel(),"Tiêu chuẩn")+" × "+i.getQuantity()).collect(java.util.stream.Collectors.joining("; "));
   if(!o.getAccessories().isEmpty())summary+="; "+o.getAccessories().stream().map(a->a.getName()+" × "+a.getQuantity()).collect(java.util.stream.Collectors.joining("; "));
   jobs.get(o.getDeliveryDate()).add(new Job(o.getDeliveryDate(),o.getDeliverySlot(),"#"+o.getId(),o.getRecipient(),summary,o.getCakeMessage(),o.getStatus().getLabel(),o.getFulfillmentMethod().getLabel(),o.getPayment().status(o.getTotal()),"/admin/orders/"+o.getId(),o.getStatus()==OrderStatus.PENDING,o.getDeliveryDate().isBefore(LocalDate.now())&&o.getStatus()!=OrderStatus.COMPLETED));
   for(var i:o.getItems())cakes.get(o.getDeliveryDate()).merge(i.getProductName()+" · "+Objects.toString(i.getOptionLabel(),"Tiêu chuẩn"),i.getQuantity(),Integer::sum);
  }
  for(var r:custom.findByReceiveDateBetweenOrderByReceiveDateAscReceiveSlotAsc(start,end)){
   if(r.getStatus()==CustomCakeStatus.CANCELLED||r.getStatus()==CustomCakeStatus.REJECTED||r.getStatus()==CustomCakeStatus.SENT||r.getStatus()==CustomCakeStatus.QUOTED||("active".equals(mode)&&r.getStatus()==CustomCakeStatus.COMPLETED))continue;
   jobs.get(r.getReceiveDate()).add(new Job(r.getReceiveDate(),r.getReceiveSlot(),"#TK"+r.getId(),r.getRecipient(),"Bánh riêng vị "+r.getFlavor()+", "+r.getServings()+" người. "+r.getDescription(),r.getCakeMessage(),r.getStatus().getLabel(),r.getFulfillmentMethod().getLabel(),r.getPayment().status(r.getQuotedTotal()),"/admin/custom-cakes/"+r.getId(),false,r.getReceiveDate().isBefore(LocalDate.now())&&r.getStatus()!=CustomCakeStatus.COMPLETED));
  }
  var days=new ArrayList<WorkDay>();for(var entry:jobs.entrySet()){entry.getValue().sort(Comparator.comparing(j->Objects.toString(j.slot(),"")));days.add(new WorkDay(entry.getKey(),entry.getValue(),cakes.get(entry.getKey()),entry.getValue().stream().filter(j->j.code().startsWith("#TK")).count()));}
  return new Schedule(start,end,mode,days,jobs.values().stream().mapToInt(List::size).sum(),cakes.values().stream().flatMap(m->m.values().stream()).mapToInt(Integer::intValue).sum(),days.stream().mapToLong(WorkDay::customJobs).sum());
 }
}
