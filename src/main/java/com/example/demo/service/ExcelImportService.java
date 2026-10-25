package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.dto.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Validator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipInputStream;

@Service
public class ExcelImportService {
 public enum Kind { PRODUCT, ACCESSORY, STOCK }
 public record Row(int number,List<String> cells,List<String> errors) implements Serializable {}
 public record Preview(Kind kind,List<String> headers,List<Row> rows,String token,String owner,LocalDateTime expiresAt) implements Serializable {public boolean isValid(){return !rows.isEmpty()&&rows.stream().allMatch(r->r.errors().isEmpty());}}
 private final AdminService admin;private final AccessoryService accessories;private final ProductRepository products;private final CategoryRepository categories;private final AccessoryRepository accessoryRepository;private final UserRepository users;private final AccountService accounts;private final Validator validator;
 public ExcelImportService(AdminService admin,AccessoryService accessories,ProductRepository products,CategoryRepository categories,AccessoryRepository accessoryRepository,UserRepository users,AccountService accounts,Validator validator){this.admin=admin;this.accessories=accessories;this.products=products;this.categories=categories;this.accessoryRepository=accessoryRepository;this.users=users;this.accounts=accounts;this.validator=validator;}
 public List<String> headers(Kind kind){return switch(kind){case PRODUCT->List.of("Tên bánh","Danh mục","Giá","Tồn ban đầu","Mô tả","Thành phần","Dị ứng","Bảo quản","Đang bán","Nổi bật");case ACCESSORY->List.of("Tên phụ kiện","Giá","Tồn ban đầu","Đang bán");case STOCK->List.of("Mã bánh","Số lượng điều chỉnh","Lý do");};}
 public byte[] template(Kind kind){try(var workbook=new XSSFWorkbook();var out=new ByteArrayOutputStream()){
  var sheet=workbook.createSheet("Du lieu");var header=sheet.createRow(0);var style=workbook.createCellStyle();var font=workbook.createFont();font.setBold(true);style.setFont(font);style.setFillForegroundColor(IndexedColors.ROSE.getIndex());style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
  var titles=headers(kind);for(int i=0;i<titles.size();i++){var cell=header.createCell(i);cell.setCellValue(titles.get(i));cell.setCellStyle(style);sheet.setColumnWidth(i,Math.min(60,Math.max(20,titles.get(i).length()+5))*256);}sheet.createFreezePane(0,1);
  var help=workbook.createSheet("Huong dan");var notes=List.of("Bóng Bakery — mẫu nhập "+kind,"Chỉ nhập dữ liệu ở sheet Du lieu, giữ nguyên tiêu đề. Tối đa 300 dòng, file .xlsx không quá 2 MB.","Giá là số nguyên VND, không có dấu phân cách. Đang bán/Nổi bật: 1 hoặc 0.","PRODUCT và ACCESSORY chỉ thêm mới, không sửa hàng đã có. Tên bánh phải kèm danh mục đã tồn tại.","STOCK điều chỉnh cộng/trừ trên tồn hiện tại, cần mã bánh và lý do. Không dùng số tồn tuyệt đối.","Không nhập công thức, macro, ảnh/video; bổ sung media sau trong admin.","Kiểm tra bản xem trước rồi xác nhận. Nếu một dòng lỗi, không nhập dòng nào.");for(int i=0;i<notes.size();i++)help.createRow(i).createCell(0).setCellValue(notes.get(i));help.setColumnWidth(0,110*256);
  workbook.write(out);return out.toByteArray();
 }catch(IOException e){throw new BusinessException("Không tạo được file mẫu Excel.");}}
 @Transactional(readOnly=true) public Preview preview(MultipartFile file,Kind kind,String email){
  requireAdmin(email);if(file==null||file.isEmpty()||file.getSize()>2*1024*1024||file.getOriginalFilename()==null||!file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".xlsx"))throw new BusinessException("Chọn file .xlsx, tối đa 2 MB.");
  List<Row> rows=new ArrayList<>();try{
   byte[] bytes=file.getBytes();inspectZip(bytes);try(var workbook=new XSSFWorkbook(new ByteArrayInputStream(bytes))){var sheet=workbook.getSheet("Du lieu");if(sheet==null)throw new BusinessException("Thiếu sheet Du lieu. Hãy tải file mẫu.");if(sheet.getLastRowNum()>300)throw new BusinessException("Mỗi lần nhập tối đa 300 dòng.");var titles=headers(kind);
    if(sheet.getRow(0)==null)throw new BusinessException("File không có tiêu đề.");for(int c=0;c<titles.size();c++)if(!cell(sheet.getRow(0).getCell(c)).equals(titles.get(c)))throw new BusinessException("Tiêu đề chưa đúng mẫu ở cột "+(c+1)+".");
    Set<String> seen=new HashSet<>();for(int i=1;i<=sheet.getLastRowNum();i++){var row=sheet.getRow(i);if(row==null)continue;List<String> cells=new ArrayList<>(),errors=new ArrayList<>();try{for(int c=0;c<titles.size();c++)cells.add(cell(row.getCell(c)));if(cells.stream().allMatch(String::isBlank))continue;validate(kind,cells);String key=kind==Kind.PRODUCT?cells.get(0).toLowerCase(Locale.ROOT)+"|"+cells.get(1).toLowerCase(Locale.ROOT):cells.get(0).toLowerCase(Locale.ROOT);if(!seen.add(key))throw new BusinessException("Dòng trùng tên/danh mục hoặc mã bánh trong file.");}catch(BusinessException e){errors.add(e.getMessage());}rows.add(new Row(i+1,List.copyOf(cells),List.copyOf(errors)));}
   }
  }catch(IOException e){throw new BusinessException("Không đọc được file Excel hợp lệ. Hãy dùng mẫu .xlsx.");}catch(org.apache.poi.ooxml.POIXMLException|IllegalArgumentException e){throw new BusinessException("File Excel bị lỗi hoặc không đúng định dạng.");}
  return new Preview(kind,headers(kind),List.copyOf(rows),UUID.randomUUID().toString(),email,LocalDateTime.now().plusMinutes(30));
 }
 private void inspectZip(byte[] bytes)throws IOException{try(var zip=new ZipInputStream(new ByteArrayInputStream(bytes))){int count=0;long size=0;Set<String> names=new HashSet<>();byte[] buffer=new byte[8192];for(var entry=zip.getNextEntry();entry!=null;entry=zip.getNextEntry()){if(++count>1000||!names.add(entry.getName())||entry.getName().toLowerCase(Locale.ROOT).contains("vbaproject"))throw new BusinessException("File có macro hoặc cấu trúc không được hỗ trợ.");for(int n=zip.read(buffer);n!=-1;n=zip.read(buffer)){size+=n;if(size>20*1024*1024)throw new BusinessException("Nội dung giải nén vượt giới hạn cho phép.");}}if(count==0)throw new BusinessException("File không phải Excel .xlsx.");}}
 private String cell(Cell c){if(c==null)return "";String value=switch(c.getCellType()){case BLANK->"";case STRING->c.getStringCellValue().strip();case NUMERIC->BigDecimal.valueOf(c.getNumericCellValue()).stripTrailingZeros().toPlainString();case BOOLEAN->c.getBooleanCellValue()?"1":"0";case FORMULA->throw new BusinessException("Không nhận ô công thức. Hãy dán giá trị trước khi nhập.");default->throw new BusinessException("Ô dữ liệu bị lỗi.");};if(value.length()>5000)throw new BusinessException("Ô dữ liệu quá dài.");return value;}
 private int integer(String s){try{return Integer.parseInt(s);}catch(NumberFormatException e){throw new BusinessException("Số lượng/mã phải là số nguyên.");}}
 private BigDecimal price(String s){try{return new BigDecimal(s);}catch(NumberFormatException e){throw new BusinessException("Giá phải là số nguyên VND, không dùng dấu phân cách.");}}
 private boolean flag(String s){if(s.equals("1"))return true;if(s.equals("0"))return false;throw new BusinessException("Đang bán/Nổi bật chỉ nhận 1 hoặc 0.");}
 private Long category(String name){return categories.findAll().stream().filter(c->c.getName().equalsIgnoreCase(name)).map(Category::getId).findFirst().orElseThrow(()->new BusinessException("Danh mục chưa tồn tại: "+name));}
 private ProductForm product(List<String> c){var f=new ProductForm();f.setName(c.get(0));f.setCategoryId(category(c.get(1)));f.setPrice(price(c.get(2)));f.setStock(integer(c.get(3)));f.setDescription(c.get(4));f.setIngredients(c.get(5));f.setAllergens(c.get(6));f.setStorageInstructions(c.get(7));f.setActive(flag(c.get(8)));f.setFeatured(flag(c.get(9)));return f;}
 private AccessoryForm accessory(List<String> c){var f=new AccessoryForm();f.setName(c.get(0));f.setPrice(price(c.get(1)));f.setStock(integer(c.get(2)));f.setActive(flag(c.get(3)));return f;}
 private void bean(Object f){var errors=validator.validate(f);if(!errors.isEmpty())throw new BusinessException(errors.stream().map(e->fieldName(e.getPropertyPath().toString())+": "+e.getMessage()).sorted().collect(java.util.stream.Collectors.joining("; ")));}
 private String fieldName(String key){return switch(key){case "name"->"Tên";case "description"->"Mô tả";case "price"->"Giá";case "stock"->"Tồn ban đầu";case "categoryId"->"Danh mục";case "ingredients"->"Thành phần";case "allergens"->"Dị ứng";case "storageInstructions"->"Bảo quản";default->"Dữ liệu";};}
 private void validate(Kind kind,List<String> c){switch(kind){
  case PRODUCT->{var f=product(c);bean(f);if(products.existsByNameIgnoreCaseAndCategoryId(f.getName(),f.getCategoryId()))throw new BusinessException("Bánh đã tồn tại trong danh mục này; không thêm trùng.");}
  case ACCESSORY->{var f=accessory(c);bean(f);if(accessoryRepository.existsByNameIgnoreCase(f.getName()))throw new BusinessException("Phụ kiện đã tồn tại; không thêm trùng.");}
  case STOCK->{long id;try{id=Long.parseLong(c.get(0));}catch(NumberFormatException e){throw new BusinessException("Mã bánh phải là số nguyên.");}var p=products.findById(id).orElseThrow(()->new BusinessException("Mã bánh không tồn tại."));int delta=integer(c.get(1));if(delta==0||delta < -1000000||delta>1000000||(long)p.getStock()+delta<0||(long)p.getStock()+delta>1000000)throw new BusinessException("Điều chỉnh làm tồn kho không hợp lệ.");if(c.get(2).isBlank()||c.get(2).length()>288)throw new BusinessException("Lý do cần từ 1 đến 288 ký tự.");}
 }}
 private User requireAdmin(String email){var u=accounts.current(email);if(u.getRole()!=Role.ADMIN)throw new BusinessException("Chỉ quản trị viên được nhập Excel.");return u;}
 @Transactional public int commit(Preview preview,String email){
  var u=requireAdmin(email);users.lockById(u.getId()).orElseThrow();if(preview==null||!preview.owner().equals(email)||!preview.expiresAt().isAfter(LocalDateTime.now())||!preview.isValid())throw new BusinessException("Bản xem trước không hợp lệ hoặc đã hết hạn. Hãy tải lại file.");
  if(preview.kind()==Kind.ACCESSORY){long added=preview.rows().stream().filter(r->flag(r.cells().get(3))).count();if(accessoryRepository.countByActiveTrue()+added>10)throw new BusinessException("Vượt 10 loại phụ kiện đang bán; chuyển một số loại sang ngừng bán trước.");}
  for(var row:preview.rows()){try{validate(preview.kind(),row.cells());switch(preview.kind()){case PRODUCT->admin.product(product(row.cells()),null,null,email);case ACCESSORY->accessories.save(accessory(row.cells()));case STOCK->admin.stock(Long.valueOf(row.cells().get(0)),integer(row.cells().get(1)),"Nhập Excel: "+row.cells().get(2),email);}}catch(BusinessException e){throw new BusinessException("Dòng "+row.number()+": "+e.getMessage()+" Chưa nhập dòng nào.");}}
  return preview.rows().size();
 }
}
