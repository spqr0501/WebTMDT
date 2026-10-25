package com.example.demo.service;
import com.example.demo.repository.CustomerOrderRepository;
import com.example.demo.entity.OrderStatus;
import org.springframework.stereotype.Service;
import java.math.*;
import java.time.*;
import java.util.*;
@Service
public class DashboardService {
 public record DayRevenue(int day,BigDecimal total,int percentage){}
 public record Report(String month,BigDecimal total,long orders,List<DayRevenue> days,long completed,long cancelled,BigDecimal average,BigDecimal cancelRate){}
 private final CustomerOrderRepository orders;private final com.example.demo.repository.CustomCakeRequestRepository custom;
 public DashboardService(CustomerOrderRepository orders,com.example.demo.repository.CustomCakeRequestRepository custom){this.orders=orders;this.custom=custom;}
 public Report report(String month){
  YearMonth m;
  try{m=month==null||month.isBlank()?YearMonth.now():YearMonth.parse(month);}catch(Exception e){throw new BusinessException("Tháng thống kê không hợp lệ.");}
  if(m.getYear()<2000||m.getYear()>2100)throw new BusinessException("Năm thống kê phải từ 2000 đến 2100.");
  var data=orders.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(m.atDay(1).atStartOfDay(),m.plusMonths(1).atDay(1).atStartOfDay());
  Map<Integer,BigDecimal> totals=new HashMap<>();BigDecimal total=BigDecimal.ZERO;
  for(var o:data)if(o.getStatus()==OrderStatus.COMPLETED){var net=o.getTotal().subtract(o.getPayment().getRefunded());totals.merge(o.getCreatedAt().getDayOfMonth(),net,BigDecimal::add);total=total.add(net);}
  var requests=custom.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(m.atDay(1).atStartOfDay(),m.plusMonths(1).atDay(1).atStartOfDay());
  for(var r:requests)if(r.getStatus()==com.example.demo.entity.CustomCakeStatus.COMPLETED&&r.getQuotedTotal()!=null){var net=r.getQuotedTotal().subtract(r.getPayment().getRefunded());totals.merge(r.getCreatedAt().getDayOfMonth(),net,BigDecimal::add);total=total.add(net);}
  BigDecimal max=totals.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE).max(BigDecimal.ONE);
  List<DayRevenue> days=new ArrayList<>();for(int d=1;d<=m.lengthOfMonth();d++){var value=totals.getOrDefault(d,BigDecimal.ZERO);days.add(new DayRevenue(d,value,value.multiply(BigDecimal.valueOf(100)).divide(max,0,RoundingMode.HALF_UP).intValue()));}
  long completed=data.stream().filter(o->o.getStatus()==OrderStatus.COMPLETED).count()+requests.stream().filter(r->r.getStatus()==com.example.demo.entity.CustomCakeStatus.COMPLETED).count();
  long cancelled=data.stream().filter(o->o.getStatus()==OrderStatus.CANCELLED).count()+requests.stream().filter(r->r.getStatus()==com.example.demo.entity.CustomCakeStatus.CANCELLED).count();
  long count=data.size()+requests.size();
  BigDecimal average=completed==0?BigDecimal.ZERO:total.divide(BigDecimal.valueOf(completed),0,RoundingMode.HALF_UP);
  BigDecimal cancelRate=count==0?BigDecimal.ZERO:BigDecimal.valueOf(cancelled*100).divide(BigDecimal.valueOf(count),1,RoundingMode.HALF_UP);
  return new Report(m.toString(),total,count,days,completed,cancelled,average,cancelRate);
 }
}
