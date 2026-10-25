package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.math.*;
import java.util.*;
@Service
public class SalesReportService {
 public record Row(String code,LocalDate date,String type,String status,boolean completed,BigDecimal total,BigDecimal refunded,BigDecimal revenue){}
 public record Day(LocalDate date,long orders,long completed,BigDecimal revenue){}
 public record Report(LocalDate from,LocalDate to,String basis,String type,long orders,long completed,long cancelled,BigDecimal gross,BigDecimal refunded,BigDecimal revenue,BigDecimal average,List<Day> days,List<Row> rows){}
 private final CustomerOrderRepository orders;private final CustomCakeRequestRepository custom;
 public SalesReportService(CustomerOrderRepository orders,CustomCakeRequestRepository custom){this.orders=orders;this.custom=custom;}
 @Transactional(readOnly=true) public Report report(LocalDate from,LocalDate to,String basis,String type){
  LocalDate start=from==null?LocalDate.now().withDayOfMonth(1):from,end=to==null?LocalDate.now():to;
  if(start.getYear()<2000||end.getYear()>2100||end.isBefore(start)||java.time.temporal.ChronoUnit.DAYS.between(start,end)>365)throw new BusinessException("Chọn khoảng từ 1 đến 366 ngày, năm từ 2000 đến 2100.");
  if(!Set.of("CREATED","COMPLETED").contains(basis)||!Set.of("ALL","STANDARD","CUSTOM").contains(type))throw new BusinessException("Bộ lọc báo cáo không hợp lệ.");
  var rows=new ArrayList<Row>();long cancelled=0;LocalDateTime a=start.atStartOfDay(),b=end.plusDays(1).atStartOfDay();
  if(!type.equals("CUSTOM"))for(var o:basis.equals("CREATED")?orders.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(a,b):orders.findByCompletedAtGreaterThanEqualAndCompletedAtLessThan(a,b)){
   boolean done=o.getStatus()==OrderStatus.COMPLETED;if(o.getStatus()==OrderStatus.CANCELLED)cancelled++;BigDecimal refund=done?o.getPayment().getRefunded():BigDecimal.ZERO;
   rows.add(new Row("#"+o.getId(),(basis.equals("CREATED")?o.getCreatedAt():o.getCompletedAt()).toLocalDate(),"Đơn thường",o.getStatus().getLabel(),done,o.getTotal(),refund,done?o.getTotal().subtract(refund):BigDecimal.ZERO));
  }
  if(!type.equals("STANDARD"))for(var r:basis.equals("CREATED")?custom.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(a,b):custom.findByCompletedAtGreaterThanEqualAndCompletedAtLessThan(a,b)){
   boolean done=r.getStatus()==CustomCakeStatus.COMPLETED;if(r.getStatus()==CustomCakeStatus.CANCELLED)cancelled++;BigDecimal total=r.getQuotedTotal()==null?BigDecimal.ZERO:r.getQuotedTotal(),refund=done?r.getPayment().getRefunded():BigDecimal.ZERO;
   rows.add(new Row("#TK"+r.getId(),(basis.equals("CREATED")?r.getCreatedAt():r.getCompletedAt()).toLocalDate(),"Bánh riêng",r.getStatus().getLabel(),done,total,refund,done?total.subtract(refund):BigDecimal.ZERO));
  }
  rows.sort(Comparator.comparing(Row::date).thenComparing(Row::code));
  BigDecimal gross=rows.stream().filter(Row::completed).map(Row::total).reduce(BigDecimal.ZERO,BigDecimal::add),refund=rows.stream().map(Row::refunded).reduce(BigDecimal.ZERO,BigDecimal::add);long completed=rows.stream().filter(Row::completed).count();
  var groups=new HashMap<LocalDate,List<Row>>();for(var row:rows)groups.computeIfAbsent(row.date(),d->new ArrayList<>()).add(row);var days=new ArrayList<Day>();for(LocalDate d=start;!d.isAfter(end);d=d.plusDays(1)){var group=groups.getOrDefault(d,List.of());days.add(new Day(d,group.size(),group.stream().filter(Row::completed).count(),group.stream().map(Row::revenue).reduce(BigDecimal.ZERO,BigDecimal::add)));}
  BigDecimal net=gross.subtract(refund);return new Report(start,end,basis,type,rows.size(),completed,cancelled,gross,refund,net,completed==0?BigDecimal.ZERO:net.divide(BigDecimal.valueOf(completed),0,RoundingMode.HALF_UP),List.copyOf(days),List.copyOf(rows));
 }
}
