package com.example.demo.service;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
@Service
public class SalesReportPdfService {
 private static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd/MM/yyyy");
 public byte[] pdf(SalesReportService.Report r){try(var doc=new PDDocument();var out=new ByteArrayOutputStream();var fontStream=getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf")){
  if(fontStream==null)throw new IOException();var font=PDType0Font.load(doc,fontStream,true);
  List<String> lines=new ArrayList<>();lines.add("BÓNG BAKERY — BÁO CÁO BÁN HÀNG");lines.add("Từ "+r.from().format(DATE)+" đến "+r.to().format(DATE));lines.add("Theo "+(r.basis().equals("CREATED")?"ngày tạo đơn":"ngày hoàn thành")+" | "+switch(r.type()){case "STANDARD"->"Đơn thường";case "CUSTOM"->"Bánh riêng";default->"Tất cả loại đơn";});
  lines.add("Số đơn/yêu cầu: "+r.orders()+" | Hoàn thành: "+r.completed()+" | Hủy: "+r.cancelled());lines.add("Giá trị hoàn thành trước hoàn tiền: "+InvoiceService.money(r.gross()));lines.add("Khoản hoàn thực tế của các đơn hoàn thành: "+InvoiceService.money(r.refunded()));lines.add("Doanh thu sau hoàn: "+InvoiceService.money(r.revenue()));lines.add("Trung bình đơn hoàn thành: "+InvoiceService.money(r.average()));lines.add(" ");lines.add("THEO NGÀY | TỔNG ĐƠN | HOÀN THÀNH | DOANH THU");
  for(var d:r.days())lines.add(d.date().format(DATE)+" | "+d.orders()+" | "+d.completed()+" | "+InvoiceService.money(d.revenue()));
  lines.add(" ");lines.add("CHI TIẾT ĐƠN TRONG BỘ LỌC");for(var row:r.rows())lines.add(row.code()+" | "+row.date().format(DATE)+" | "+row.status()+" | Doanh thu: "+InvoiceService.money(row.revenue()));
  lines.add(" ");lines.add("Khoản hoàn được trừ theo số đã ghi nhận tại thời điểm xuất. Báo cáo theo tập đơn, không phải sổ thu chi hay lợi nhuận. Đơn cũ không có giờ hoàn thành chỉ xem được theo ngày tạo.");
  PDPageContentStream stream=null;float y=0;int page=0;
  try{for(var text:lines)for(String line:wrap(text,font,9,510)){
   if(stream==null||y<55){if(stream!=null)stream.close();var sheet=new PDPage(PDRectangle.A4);doc.addPage(sheet);stream=new PDPageContentStream(doc,sheet);page++;draw(stream,font,"Bóng Bakery | Báo cáo | Trang "+page,42,28,8);y=790;}
   draw(stream,font,line,42,y,9);y-=16;
  }}finally{if(stream!=null)stream.close();}doc.getDocumentInformation().setTitle("Báo cáo Bóng Bakery "+r.from()+" — "+r.to());doc.save(out);return out.toByteArray();
 }catch(IOException e){throw new BusinessException("Chưa tạo được PDF báo cáo. Vui lòng thử lại.");}}
 private String clean(String text,PDFont font)throws IOException{StringBuilder b=new StringBuilder();for(int cp:text.replaceAll("[\\p{Cntrl}\\p{Cf}]"," ").codePoints().toArray()){String s=new String(Character.toChars(cp));try{font.encode(s);b.append(s);}catch(IllegalArgumentException e){b.append('?');}}return b.toString();}
 private List<String> wrap(String text,PDFont font,int size,int width)throws IOException{List<String> result=new ArrayList<>();StringBuilder line=new StringBuilder();for(int cp:clean(text,font).codePoints().toArray()){String ch=new String(Character.toChars(cp));if(line.length()>0&&font.getStringWidth(line.toString()+ch)*size/1000>width){result.add(line.toString());line.setLength(0);}line.append(ch);}result.add(line.toString());return result;}
 private void draw(PDPageContentStream stream,PDFont font,String text,float x,float y,int size)throws IOException{stream.beginText();stream.setFont(font,size);stream.newLineAtOffset(x,y);stream.showText(clean(text,font));stream.endText();}
}
