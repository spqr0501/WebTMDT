package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.google.zxing.*;
import com.google.zxing.client.j2se.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class PickupService {
 public record Ticket(String code,String kind,Long id,String recipient,String phone,String status,java.time.LocalDate date,String slot,java.math.BigDecimal remaining,String detailUrl,boolean ready,boolean completed){}
 private final CustomerOrderRepository orders; private final CustomCakeRequestRepository custom;
 private final OrderService orderService; private final CustomCakeService customService;
 public PickupService(CustomerOrderRepository orders,CustomCakeRequestRepository custom,OrderService orderService,CustomCakeService customService){this.orders=orders;this.custom=custom;this.orderService=orderService;this.customService=customService;}
 public String orderCode(CustomerOrder o){pickup(o.getFulfillmentMethod());return code("O",o.getId(),o.getCheckoutToken());}
 public String customCode(CustomCakeRequest r){pickup(r.getFulfillmentMethod());return code("C",r.getId(),r.getToken());}
 private void pickup(FulfillmentMethod method){if(method!=FulfillmentMethod.PICKUP)throw new BusinessException("Đơn này không nhận tại cửa hàng.");}
 private String code(String type,Long id,String secret){
  String payload="BONG-PICKUP:"+type+":"+id;
  try{var mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return payload+":"+Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));}
  catch(java.security.GeneralSecurityException e){throw new IllegalStateException("Không tạo được mã nhận bánh",e);}
 }
 private void verify(String actual,String expected){if(!MessageDigest.isEqual(actual.getBytes(StandardCharsets.UTF_8),expected.getBytes(StandardCharsets.UTF_8)))throw new BusinessException("Mã nhận bánh không hợp lệ.");}
 @Transactional(readOnly=true) public Ticket lookup(String raw){
  String value=raw==null?"":raw.strip();if(!value.matches("BONG-PICKUP:[OC]:[1-9][0-9]{0,17}:[A-Za-z0-9_-]{43}"))throw new BusinessException("Mã nhận bánh không đúng định dạng.");
  var parts=value.split(":");Long id=Long.valueOf(parts[2]);
  if(parts[1].equals("O")){
   var o=orders.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn nhận bánh."));verify(value,orderCode(o));
   return new Ticket(value,"Đơn bánh",id,o.getRecipient(),o.getPhone(),o.getStatus().getLabel(),o.getDeliveryDate(),o.getDeliverySlot(),o.getPayment().remaining(o.getTotal()),"/admin/orders/"+id,o.getStatus()==OrderStatus.READY_FOR_PICKUP,o.getStatus()==OrderStatus.COMPLETED);
  }
  var r=custom.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn nhận bánh."));verify(value,customCode(r));
  return new Ticket(value,"Bánh thiết kế",id,r.getRecipient(),r.getPhone(),r.getStatus().getLabel(),r.getReceiveDate(),r.getReceiveSlot(),r.getQuotedTotal()==null?r.getBudget():r.getPayment().remaining(r.getQuotedTotal()),"/admin/custom-cakes/"+id,r.getStatus()==CustomCakeStatus.READY,r.getStatus()==CustomCakeStatus.COMPLETED);
 }
 @Transactional public Ticket complete(String raw,String actor){
  // Khóa cùng bản ghi với đối soát và cập nhật trạng thái, chống giao bánh hai lần.
  Ticket ticket=lookup(raw);var parts=ticket.code().split(":");
  if(parts[1].equals("O")){
   var o=orders.lockById(ticket.id()).orElseThrow();verify(ticket.code(),orderCode(o));
   if(o.getStatus()!=OrderStatus.COMPLETED){if(o.getStatus()!=OrderStatus.READY_FOR_PICKUP)throw new BusinessException("Bánh chưa sẵn sàng để bàn giao.");orderService.transition(o.getId(),OrderStatus.COMPLETED,actor);}
  }else{
   var r=custom.lockById(ticket.id()).orElseThrow();verify(ticket.code(),customCode(r));
   if(r.getStatus()!=CustomCakeStatus.COMPLETED){if(r.getStatus()!=CustomCakeStatus.READY)throw new BusinessException("Bánh chưa sẵn sàng để bàn giao.");customService.update(r.getId(),CustomCakeStatus.COMPLETED,null,"");}
  }
  return lookup(raw);
 }
 public byte[] png(String value){try{var matrix=new QRCodeWriter().encode(value,BarcodeFormat.QR_CODE,360,360,Map.of(EncodeHintType.MARGIN,4));var out=new ByteArrayOutputStream();MatrixToImageWriter.writeToStream(matrix,"PNG",out);return out.toByteArray();}catch(IOException|WriterException e){throw new IllegalStateException("Không tạo được ảnh QR",e);}}
 public String read(MultipartFile file){
  if(file==null||file.isEmpty()||file.getSize()>5*1024*1024)throw new BusinessException("Chọn ảnh QR JPG/PNG, tối đa 5 MB.");
  try(var input=file.getInputStream();var stream=ImageIO.createImageInputStream(input)){
   var readers=ImageIO.getImageReaders(stream);if(!readers.hasNext())throw new BusinessException("Không đọc được ảnh QR.");var reader=readers.next();
   try{reader.setInput(stream);String format=reader.getFormatName();if(!format.equalsIgnoreCase("png")&&!format.equalsIgnoreCase("jpeg"))throw new BusinessException("Chỉ nhận ảnh JPG/PNG.");if((long)reader.getWidth(0)*reader.getHeight(0)>16000000)throw new BusinessException("Ảnh tối đa 16 triệu điểm ảnh.");var bitmap=new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(reader.read(0))));return new MultiFormatReader().decode(bitmap,Map.of(DecodeHintType.POSSIBLE_FORMATS,List.of(BarcodeFormat.QR_CODE),DecodeHintType.TRY_HARDER,true)).getText();}
   finally{reader.dispose();}
  }catch(IOException|NotFoundException e){throw new BusinessException("Không tìm thấy QR rõ nét. Hãy chọn ảnh khác hoặc dán mã nhận bánh.");}
 }
}
