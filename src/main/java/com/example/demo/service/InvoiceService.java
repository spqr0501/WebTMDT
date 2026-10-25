package com.example.demo.service;
import com.example.demo.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.io.*;
import java.text.Normalizer;
import java.text.NumberFormat;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;

@Service
public class InvoiceService {
 public record Line(String name,int quantity,BigDecimal unitPrice,BigDecimal total){}
 public record Transaction(String type,BigDecimal amount,String reference,String actor,LocalDateTime createdAt){}
 public record Invoice(String code,String title,String buyer,String phone,String address,String fulfillment,
   LocalDateTime createdAt,String schedule,String status,boolean cancelled,String note,String cakeMessage,
   List<Line> lines,BigDecimal subtotal,BigDecimal discount,BigDecimal shipping,BigDecimal total,
   String paymentPlan,BigDecimal upfront,BigDecimal paid,BigDecimal refunded,BigDecimal remaining,List<Transaction> transactions){}
 private final PaymentService payments;
 @Value("${app.shop.address:}") private String shopAddress;
 @Value("${app.shop.phone:}") private String shopPhone;
 public InvoiceService(PaymentService payments){this.payments=payments;}
 public Invoice order(CustomerOrder o){
  List<Line> lines=new ArrayList<>();for(OrderItem i:o.getItems())lines.add(new Line(i.getProductName()+(i.getOptionLabel()==null?"":" - "+i.getOptionLabel()),i.getQuantity(),i.getUnitPrice(),i.getLineTotal()));for(OrderAccessory a:o.getAccessories())lines.add(new Line(a.getName()+" - Phụ kiện",a.getQuantity(),a.getUnitPrice(),a.getLineTotal()));
  return new Invoice(String.format(Locale.ROOT,"BB-%06d",o.getId()),"HÓA ĐƠN BÁN HÀNG",o.getRecipient(),o.getPhone(),o.getDeliveryAddress(),o.getFulfillmentMethod().getLabel(),o.getCreatedAt(),schedule(o.getDeliveryDate(),o.getDeliverySlot()),o.getStatus().getLabel(),o.getStatus()==OrderStatus.CANCELLED,o.getNote(),o.getCakeMessage(),List.copyOf(lines),o.getSubtotal(),o.getDiscount(),o.getShippingFee(),o.getTotal(),o.getPayment().getPlan().getLabel(),o.getPayment().getRequiredUpfront(),o.getPayment().getPaid(),o.getPayment().getRefunded(),o.getPayment().remaining(o.getTotal()),history(payments.orderHistory(o.getId())));
 }
 public Invoice custom(CustomCakeRequest r){
  if(r.getQuotedTotal()==null)throw new BusinessException("Bánh thiết kế riêng chưa có báo giá để xuất chứng từ.");
  String title=r.getStatus()==CustomCakeStatus.QUOTED?"BÁO GIÁ BÁNH THIẾT KẾ":"HÓA ĐƠN BÁNH THIẾT KẾ";
  String name="Bánh thiết kế riêng - "+r.getFlavor()+" - "+r.getServings()+" người. "+(r.getAdminReply()==null?r.getDescription():r.getAdminReply());
  return new Invoice(String.format(Locale.ROOT,"BB-TK-%06d",r.getId()),title,r.getRecipient(),r.getPhone(),r.getAddress(),r.getFulfillmentMethod().getLabel(),r.getCreatedAt(),schedule(r.getReceiveDate(),r.getReceiveSlot()),r.getStatus().getLabel(),r.getStatus()==CustomCakeStatus.CANCELLED||r.getStatus()==CustomCakeStatus.REJECTED,r.getDescription(),r.getCakeMessage(),List.of(new Line(name,1,r.getQuotedTotal(),r.getQuotedTotal())),r.getQuotedTotal(),BigDecimal.ZERO,BigDecimal.ZERO,r.getQuotedTotal(),r.getPayment().getPlan().getLabel(),r.getPayment().getRequiredUpfront(),r.getPayment().getPaid(),r.getPayment().getRefunded(),r.getPayment().remaining(r.getQuotedTotal()),history(payments.customHistory(r.getId())));
 }
 private List<Transaction> history(List<PaymentRecord> records){return records.stream().map(r->new Transaction(r.getKind()==PaymentRecord.Kind.RECEIPT?"Nhận tiền":"Hoàn tiền",r.getAmount(),r.getReference(),r.getActor(),r.getCreatedAt())).toList();}
 private String schedule(LocalDate date,String slot){return date==null?"Chưa ghi nhận lịch nhận":date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))+" - "+slot;}
 public static String money(BigDecimal value){return NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(value)+" đ";}
 public byte[] pdf(Invoice invoice){
  try(PDDocument doc=new PDDocument();ByteArrayOutputStream out=new ByteArrayOutputStream()){
   PDType0Font regular=font(doc,"NotoSans-Regular.ttf"),bold=font(doc,"NotoSans-Bold.ttf");
   doc.getDocumentInformation().setTitle(invoice.title()+" "+invoice.code());doc.getDocumentInformation().setAuthor("Bóng Bakery");
   try(Layout page=new Layout(doc,regular,bold)){
    page.paragraph(invoice.title(),17,true);page.paragraph("Mã chứng từ: "+invoice.code()+" | Ngày đặt: "+invoice.createdAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),9,false);
    page.paragraph("Trạng thái: "+invoice.status(),10,true);page.gap(9);
    page.paragraph("THÔNG TIN KHÁCH HÀNG",10,true);page.paragraph(invoice.buyer()+" | "+invoice.phone(),10,false);page.paragraph(invoice.fulfillment()+": "+invoice.address(),10,false);page.paragraph("Lịch nhận: "+invoice.schedule(),10,false);page.gap(12);
    page.tableHeader();for(Line line:invoice.lines())page.tableRow(line);
    page.gap(12);page.summary("Tiền hàng / báo giá",money(invoice.subtotal()),false);page.summary("Giảm giá","-"+money(invoice.discount()),false);page.summary("Phí giao hàng",money(invoice.shipping()),false);page.summary("TỔNG CỘNG",money(invoice.total()),true);
    page.gap(12);page.paragraph("THANH TOÁN",10,true);page.paragraph("Lựa chọn: "+invoice.paymentPlan(),10,false);page.summary("Yêu cầu trả trước",money(invoice.upfront()),false);page.summary("Đã nhận (sau hoàn tiền)",money(invoice.paid()),false);page.summary("Tổng đã hoàn thực tế",money(invoice.refunded()),false);page.summary(invoice.cancelled()?"Số đang chờ hoàn":"Còn phải trả sau khoản đã hoàn",money(invoice.cancelled()?invoice.paid():invoice.remaining()),true);
    if(invoice.cancelled())page.paragraph("ĐƠN ĐÃ HỦY / KHÔNG THỂ ĐÁP ỨNG. Không tiếp tục thanh toán; khoản hoàn được cửa hàng đối soát riêng.",9,true);
    if(invoice.cakeMessage()!=null&&!invoice.cakeMessage().isBlank()){page.gap(8);page.paragraph("Lời chúc: "+invoice.cakeMessage(),9,false);}
    if(invoice.note()!=null&&!invoice.note().isBlank()){page.gap(5);page.paragraph("Ghi chú: "+invoice.note(),9,false);}
    if(!invoice.transactions().isEmpty()){page.gap(12);page.paragraph("LỊCH SỬ ĐỐI SOÁT",10,true);for(Transaction transaction:invoice.transactions())page.paragraph(transaction.createdAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))+" | "+transaction.type()+" "+money(transaction.amount())+" | "+transaction.reference(),8,false);}
    page.gap(15);page.paragraph("Số tiền đã nhận chỉ phản ánh các giao dịch cửa hàng đã xác nhận. Phiếu đơn hàng không tự xác nhận chuyển khoản.",8,false);page.paragraph("Chứng từ bán hàng nội bộ của Bóng Bakery, không phải hóa đơn điện tử VAT.",8,false);
   }
   for(int i=0;i<doc.getNumberOfPages();i++)try(PDPageContentStream footer=new PDPageContentStream(doc,doc.getPage(i),PDPageContentStream.AppendMode.APPEND,true,true)){footer.setNonStrokingColor(new java.awt.Color(145,103,124));footer.beginText();footer.setFont(regular,8);footer.newLineAtOffset(42,24);footer.showText("Bóng Bakery | "+invoice.code()+" | Trang "+(i+1)+" / "+doc.getNumberOfPages());footer.endText();}
   doc.save(out);return out.toByteArray();
  }catch(IOException e){throw new BusinessException("Chưa tạo được PDF. Vui lòng thử lại hoặc liên hệ cửa hàng.");}
 }
 private PDType0Font font(PDDocument doc,String name)throws IOException{try(InputStream in=getClass().getResourceAsStream("/fonts/"+name)){if(in==null)throw new IOException("Thiếu phông PDF");return PDType0Font.load(doc,in,true);}}
 private final class Layout implements AutoCloseable {
  private final PDDocument doc;private final PDFont regular,bold;private PDPageContentStream stream;private float y;
  Layout(PDDocument doc,PDFont regular,PDFont bold)throws IOException{this.doc=doc;this.regular=regular;this.bold=bold;newPage();}
  private void newPage()throws IOException{
   if(stream!=null)stream.close();PDPage page=new PDPage(PDRectangle.A4);doc.addPage(page);stream=new PDPageContentStream(doc,page);stream.setNonStrokingColor(new java.awt.Color(255,236,245));stream.addRect(0,774,PDRectangle.A4.getWidth(),68);stream.fill();text("Bóng Bakery",42,808,19,true);String contact=(shopAddress==null||shopAddress.isBlank()?"Tiệm bánh ngọt":shopAddress)+(shopPhone==null||shopPhone.isBlank()?"":" | "+shopPhone);text(shorten(contact,92),42,790,8,false);y=747;
  }
  private boolean ensure(float height)throws IOException{if(y-height<52){newPage();return true;}return false;}
  void gap(float height)throws IOException{ensure(height);y-=height;}
  private String clean(String value){return Normalizer.normalize(value==null?"":value,Normalizer.Form.NFC).replaceAll("[\\p{Cntrl}\\p{Cf}]"," ").replaceAll("\\s+"," ").strip();}
  private String supported(String input,PDFont font)throws IOException{StringBuilder s=new StringBuilder();for(int cp:clean(input).codePoints().toArray()){String c=new String(Character.toChars(cp));try{font.encode(c);s.append(c);}catch(IllegalArgumentException e){s.append('?');}}return s.toString();}
  private float width(String value,float size,boolean strong)throws IOException{return (strong?bold:regular).getStringWidth(value)*size/1000;}
  private List<String> wrap(String value,float max,float size,boolean strong)throws IOException{
   String text=supported(value,strong?bold:regular);List<String> lines=new ArrayList<>();StringBuilder current=new StringBuilder();
   for(String word:text.split(" ")){String candidate=current.length()==0?word:current+" "+word;if(width(candidate,size,strong)<=max){current.setLength(0);current.append(candidate);continue;}if(current.length()>0){lines.add(current.toString());current.setLength(0);}for(int cp:word.codePoints().toArray()){String c=new String(Character.toChars(cp));if(current.length()>0&&width(current+c,size,strong)>max){lines.add(current.toString());current.setLength(0);}current.append(c);}}
   if(current.length()>0)lines.add(current.toString());if(lines.isEmpty())lines.add("");return lines;
  }
  private void text(String value,float x,float baseline,float size,boolean strong)throws IOException{stream.setNonStrokingColor(new java.awt.Color(78,44,63));stream.beginText();stream.setFont(strong?bold:regular,size);stream.newLineAtOffset(x,baseline);stream.showText(supported(value,strong?bold:regular));stream.endText();}
  private void right(String value,float right,float baseline,float size,boolean strong)throws IOException{String safe=supported(value,strong?bold:regular);text(safe,right-width(safe,size,strong),baseline,size,strong);}
  void paragraph(String value,float size,boolean strong)throws IOException{for(String line:wrap(value,511,size,strong)){ensure(size*1.65f);text(line,42,y,size,strong);y-=size*1.65f;}}
  void summary(String label,String value,boolean strong)throws IOException{ensure(23);text(label,42,y,strong?11:10,strong);right(value,553,y,strong?12:10,strong);y-=23;}
  void tableHeader()throws IOException{ensure(30);stream.setNonStrokingColor(new java.awt.Color(255,240,247));stream.addRect(42,y-8,511,25);stream.fill();text("Sản phẩm / nội dung",49,y,9,true);text("SL",310,y,9,true);right("Đơn giá",434,y,9,true);right("Thành tiền",548,y,9,true);y-=30;}
  void tableRow(Line line)throws IOException{List<String> names=wrap(line.name(),247,9,false);int offset=0;while(offset<names.size()){if(ensure(35))tableHeader();int available=Math.max(1,(int)((y-58)/15));int count=Math.min(available,names.size()-offset);float base=y;if(offset==0){text(String.valueOf(line.quantity()),312,base,9,false);right(money(line.unitPrice()),434,base,9,false);right(money(line.total()),548,base,9,true);}for(int i=0;i<count;i++){text(names.get(offset+i),49,y,9,false);y-=15;}offset+=count;if(offset<names.size()){newPage();tableHeader();}}y-=9;stream.setStrokingColor(new java.awt.Color(239,218,230));stream.setLineWidth(.5f);stream.moveTo(42,y+4);stream.lineTo(553,y+4);stream.stroke();}
  @Override public void close()throws IOException{if(stream!=null)stream.close();}
 }
 private static String shorten(String value,int max){return value.length()>max?value.substring(0,max-3)+"...":value;}
}
