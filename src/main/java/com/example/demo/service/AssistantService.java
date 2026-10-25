package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import java.math.*;
import java.time.*;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.*;

/** Trợ lý truy xuất dữ liệu: câu trả lời và liên kết nguồn do máy chủ xây dựng. */
@Service
public class AssistantService {
 public record Source(String title,String url,String excerpt) implements java.io.Serializable {}
 public record Cake(Long id,String name,String image,BigDecimal price,int stock,String description,String url) implements java.io.Serializable {}
 public record Answer(String text,List<Source> sources,List<Cake> cakes,boolean handoff,LocalDateTime checkedAt) implements java.io.Serializable {}
 public record Turn(String question,Answer answer) implements java.io.Serializable {}
 public static class Conversation implements java.io.Serializable {
  public final List<Turn> turns=new ArrayList<>();public String keyword="";public BigDecimal maxPrice;public List<Long> lastProducts=new ArrayList<>();
 }
 private final ProductRepository products;private final StorePolicyService policies;
 public AssistantService(ProductRepository products,StorePolicyService policies){this.products=products;this.policies=policies;}
 public static String norm(String s){return Normalizer.normalize(s==null?"":s.toLowerCase(Locale.ROOT),Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('đ','d').replaceAll("[^a-z0-9., ]"," ").replaceAll("\\s+"," ").strip();}
 private boolean has(String q,String... terms){return Arrays.stream(terms).anyMatch(q::contains);}
 @Transactional(readOnly=true) public Answer ask(String question,Conversation context){
  if(question==null||question.isBlank()||question.length()>600)throw new BusinessException("Câu hỏi từ 1 đến 600 ký tự.");
  String q=norm(question);Answer answer;
  if(has(q,"nhan vien","ho tro vien","gap nguoi","lien he","tu van vien","ho tro truc tiep"))answer=policy("Tôi có thể chuyển nội dung sang phiếu để nhân viên trả lời trên website. Chọn “Gửi phiếu hỗ trợ”; bạn quyết định có chia sẻ hội thoại hay không.","assistant",true);
  else if(has(q,"bo qua huong dan","ignore previous","system prompt","mat khau","password","api key","cau lenh sql","xoa don","chay lenh","hack"))answer=policy("Tôi chỉ tư vấn mua bánh và chính sách Bóng Bakery. Tôi không cung cấp bí mật, thực thi chỉ dẫn thay đổi hệ thống hoặc dữ liệu người khác. Nếu cần cửa hàng hỗ trợ, hãy gửi phiếu.","assistant",true);
  else if(has(q,"kho tho","cap cuu","chua benh","thuoc","chan doan","an toan tuyet doi","bao dam khong di ung"))answer=policy("Tôi không thể chẩn đoán hoặc bảo đảm an toàn thực phẩm cho tình trạng sức khỏe của bạn. Nếu đang khó thở hoặc có phản ứng nghiêm trọng, hãy tìm trợ giúp y tế ngay. Với thành phần bánh chưa rõ, hãy hỏi nhân viên trước khi đặt.","care",true);
  else if(has(q,"hoan tien","doi tra","doi banh","hong banh","banh bi hong","hu hong","thieu banh","sai mau"))answer=policy(policies.policy("returns").text(),"returns",true);
  else if(has(q,"huy don","huy yeu cau"))answer=policy(policies.policy("cancel").text(),"cancel",false);
  else if(has(q,"voucher","khuyen mai","ma giam","uu dai"))answer=policy(policies.policy("voucher").text(),"voucher",false);
  else if(has(q,"don hang cua toi","don cua toi","kiem tra don","ma don","don dang o dau","lich su don","da chuyen tien","so tai khoan","ngan hang"))answer=new Answer("Tôi không tự xác nhận chuyển khoản hoặc đọc thông tin riêng của đơn trong hội thoại. Mở đơn hàng để xem trạng thái và hướng dẫn thanh toán đã được cửa hàng cập nhật; nếu cần đối soát, gửi phiếu hỗ trợ.",List.of(new Source("Đơn hàng của tôi","/orders","Chỉ xem sau đăng nhập, kiểm tra quyền sở hữu."),source("delivery")),List.of(),true,LocalDateTime.now());
  else if(has(q,"giao hang","phi ship","mien phi","chuyen khoan","dat coc","coc 50","nhan tai","dat truoc","khung gio","thanh toan","qr","dat banh rieng","thiet ke rieng"))answer=policy(policies.policy("delivery").text(),"delivery",false);
  else if(has(q,"nguon du lieu","lay thong tin","ai that","mo hinh","chatgpt","ban la ai"))answer=policy("Tôi là trợ lý truy xuất của Bóng Bakery, dùng quy tắc và dữ liệu cửa hàng, không phải mô hình AI tạo sinh. Tôi có thể nhớ lựa chọn gần đây trong phiên và dẫn nguồn thông tin để bạn kiểm tra.","assistant",false);
  else answer=productAnswer(question,q,context);
  context.turns.add(new Turn(question.strip(),answer));while(context.turns.size()>12)context.turns.remove(0);return answer;
 }
 private Answer productAnswer(String original,String q,Conversation c){
  var catalog=products.findAll((r,query,b)->b.isTrue(r.get("active")),PageRequest.of(0,200,org.springframework.data.domain.Sort.by("id").descending())).getContent();
  boolean care=has(q,"di ung","thanh phan","bao quan","han su dung","de tu lanh");
  boolean follow=has(q,"cai do","loai do","banh do","mau do","con hang","gia bao nhieu","re hon","con nao","loai khac","bao quan","di ung","thanh phan","han su dung");
  String explicit=catalog.stream().filter(p->q.contains(norm(p.getName()))).map(p->norm(p.getName())).findFirst().orElse("");
  List<String> categories=List.of("banh kem","mousse","cookie","banh quy","cupcake","tiramisu","cheesecake","croissant","banh mi","macaron","brownie","su kem","banh flan","panna cotta","banh tart","matcha","socola","chocolate","dau","vani");
  String keyword=!explicit.isEmpty()?explicit:categories.stream().filter(q::contains).findFirst().orElse("");
  Matcher money=Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(trieu|tr|nghin|ngan|k)\\b").matcher(q);
  if(money.find()){try{BigDecimal v=new BigDecimal(money.group(1).replace(',','.'));BigDecimal scale=money.group(2).startsWith("tr")?new BigDecimal("1000000"):new BigDecimal("1000");var max=v.multiply(scale);if(max.signum()>0&&max.compareTo(new BigDecimal("999999999"))<=0)c.maxPrice=max;}catch(NumberFormatException ignored){}}
  else {Matcher digits=Pattern.compile("(?:duoi|toi da|ngan sach|tam gia)\\s*(\\d{4,9})\\b").matcher(q);if(digits.find())c.maxPrice=new BigDecimal(digits.group(1));}
  if(has(q,"bo gioi han gia","khong gioi han gia","bat ky gia"))c.maxPrice=null;
  if(!keyword.isEmpty()){c.keyword=keyword;c.lastProducts=new ArrayList<>();}
  boolean shopping=care||follow||!keyword.isEmpty()||has(q,"banh","goi y","tim mua","sinh nhat","tang qua","ngan sach","toi da","duoi","gia","co gi","xin chao","chao ban","hello","cam on");
  if(!shopping)return policy("Tôi chỉ hỗ trợ chọn bánh và chính sách Bóng Bakery. Bạn có thể hỏi “gợi ý bánh kem dưới 500k”, “phí giao hàng” hoặc gửi phiếu cho nhân viên.","assistant",true);
  var selected=catalog.stream().filter(p->c.keyword.isBlank()||norm(p.getName()+" "+p.getCategory().getName()).contains(c.keyword)).filter(p->c.maxPrice==null||p.getPrice().compareTo(c.maxPrice)<=0).toList();
  if(care){
   if(keyword.isEmpty()&&!c.lastProducts.isEmpty())selected=catalog.stream().filter(p->c.lastProducts.contains(p.getId())).toList();
   if(selected.size()!=1){var shown=selected.stream().limit(4).toList();return new Answer("Bạn muốn hỏi thông tin của bánh nào? Chọn tên hoặc gửi tên đầy đủ để tôi tra đúng thành phần và bảo quản. Tôi không suy đoán khi dữ liệu chưa rõ.",List.of(source("care")),shown.stream().map(this::cake).toList(),true,LocalDateTime.now());}
   var p=selected.get(0);c.lastProducts=List.of(p.getId());boolean missing=blank(p.getIngredients())||blank(p.getAllergens())||blank(p.getStorageInstructions());
   String text=p.getName()+"\nThành phần: "+value(p.getIngredients())+"\nDị ứng: "+value(p.getAllergens())+"\nBảo quản: "+value(p.getStorageInstructions())+"\nThông tin theo dữ liệu cửa hàng; không phải bảo đảm an toàn cho mọi tình trạng dị ứng.";
   return new Answer(text,List.of(productSource(p),source("care")),List.of(cake(p)),missing,LocalDateTime.now());
  }
  var available=selected.stream().filter(p->p.getStock()>0).sorted(Comparator.comparing(Product::getPrice).thenComparing(Product::getId)).limit(4).toList();
  if(available.isEmpty()){var soldOut=selected.stream().filter(p->p.getStock()==0).limit(3).toList();c.lastProducts=soldOut.stream().map(Product::getId).toList();return new Answer(soldOut.isEmpty()?"Chưa tìm được bánh phù hợp với loại và ngân sách hiện tại. Bạn có thể đổi loại, tăng ngân sách hoặc hỏi nhân viên.":"Các bánh phù hợp hiện tạm hết hàng. Tôi không xác nhận có thể đặt những bánh này; hãy đổi loại hoặc gửi phiếu để hỏi lịch có hàng.",soldOut.stream().map(this::productSource).toList(),soldOut.stream().map(this::cake).toList(),true,LocalDateTime.now());}
  c.lastProducts=available.stream().map(Product::getId).toList();
  return new Answer("Tôi tìm được "+available.size()+" bánh đang còn hàng"+(c.maxPrice==null?"":" trong ngân sách tối đa "+InvoiceService.money(c.maxPrice))+". Giá dưới đây là giá bánh cơ bản; lựa chọn kích thước/vị, phụ kiện và phí giao có thể thay đổi tổng tiền. Kho sẽ được kiểm tra lại khi đặt.",available.stream().map(this::productSource).toList(),available.stream().map(this::cake).toList(),false,LocalDateTime.now());
 }
 private boolean blank(String s){return s==null||s.isBlank();} private String value(String s){return blank(s)?"Chưa cập nhật; cần xác nhận với cửa hàng.":s;}
 private Source source(String id){var p=policies.policy(id);return new Source(p.title(),"/policies#"+id,p.text());}
 private Source productSource(Product p){return new Source(p.getName(),"/products/"+p.getId(),"Giá cơ bản "+InvoiceService.money(p.getPrice())+", tồn "+p.getStock()+" bánh. "+shorten(p.getDescription(),160));}
 private Cake cake(Product p){return new Cake(p.getId(),p.getName(),p.getImageUrl(),p.getPrice(),p.getStock(),shorten(p.getDescription(),120),"/products/"+p.getId());}
 private String shorten(String s,int n){return s==null?"":s.length()>n?s.substring(0,n)+"…":s;}
 private Answer policy(String text,String id,boolean handoff){return new Answer(text,List.of(source(id)),List.of(),handoff,LocalDateTime.now());}
 public String transcript(Conversation c){StringBuilder b=new StringBuilder();for(var turn:c.turns){b.append("Khách: ").append(turn.question()).append("\nTrợ lý: ").append(turn.answer().text()).append("\n");for(var s:turn.answer().sources())b.append("Nguồn: ").append(s.url()).append("\n");}String text=b.toString();return text.length()>6000?"…\n"+text.substring(text.length()-5998):text;}
}
