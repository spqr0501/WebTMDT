package com.example.demo.controller;
import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.*;
import org.springframework.beans.BeanUtils;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.security.Principal;
@Controller @RequestMapping("/admin")
public class AdminController {
 @org.springframework.beans.factory.annotation.Autowired private DeliveryService deliveries;
 private final AfterSalesRepository afterSales;private final SupportTicketRepository supportTickets;private final PaymentService payments;private final CustomCakeRequestRepository custom;private final ProductRepository products;private final CategoryRepository categories;private final VoucherRepository vouchers;private final CustomerOrderRepository orders;private final UserRepository users;private final OrderItemRepository items;private final AdminService admin;private final OrderService orderService;private final AccountService accounts;private final DashboardService dashboard;private final OptionService optionService;private final ProductOptionRepository options;private final StockMovementRepository movements;
 public AdminController(ProductRepository products,CategoryRepository categories,VoucherRepository vouchers,CustomerOrderRepository orders,UserRepository users,OrderItemRepository items,AdminService admin,OrderService orderService,AccountService accounts,DashboardService dashboard,OptionService optionService,ProductOptionRepository options,StockMovementRepository movements,CustomCakeRequestRepository custom,PaymentService payments,AfterSalesRepository afterSales,SupportTicketRepository supportTickets){this.afterSales=afterSales;this.supportTickets=supportTickets;this.payments=payments;this.custom=custom;this.dashboard=dashboard;this.optionService=optionService;this.options=options;this.movements=movements;this.products=products;this.categories=categories;this.vouchers=vouchers;this.orders=orders;this.users=users;this.items=items;this.admin=admin;this.orderService=orderService;this.accounts=accounts;}
 @GetMapping({"","/"}) public String dashboard(@RequestParam(required=false) String month,Model m){m.addAttribute("returnPending",afterSales.countByStatus(AfterSalesRequest.Status.REQUESTED));m.addAttribute("supportPending",supportTickets.countByStatus(com.example.demo.entity.SupportTicket.Status.OPEN));m.addAttribute("report",dashboard.report(month));m.addAttribute("revenue",orders.revenue().add(custom.revenue()));m.addAttribute("orderCount",orders.count()+custom.count());m.addAttribute("pending",orders.countByStatus(OrderStatus.PENDING));m.addAttribute("customPending",custom.findByStatus(CustomCakeStatus.SENT,PageRequest.of(0,1)).getTotalElements());m.addAttribute("lowStock",products.findByActiveTrueAndStockLessThanOrderByStockAsc(5));m.addAttribute("outStock",products.findByActiveTrueAndStockLessThanOrderByStockAsc(1).size());m.addAttribute("bestSellers",items.bestSellers(PageRequest.of(0,5)));return "admin/dashboard";}
 @GetMapping("/products") public String products(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="all") String stock,Model m){
  stock=stock.strip().replaceAll(",+$","");
  if(!java.util.Set.of("all","low","out","warning").contains(stock))throw new BusinessException("Bộ lọc tồn kho không hợp lệ.");
  org.springframework.data.jpa.domain.Specification<Product> spec=(r,q,b)->b.conjunction();
  if(!stock.equals("all"))spec=spec.and((r,q,b)->b.isTrue(r.get("active")));
  if(stock.equals("low"))spec=spec.and((r,q,b)->b.between(r.get("stock"),1,4));
  if(stock.equals("out"))spec=spec.and((r,q,b)->b.equal(r.get("stock"),0));
  if(stock.equals("warning"))spec=spec.and((r,q,b)->b.lessThan(r.get("stock"),5));
  m.addAttribute("stock",stock);m.addAttribute("page",products.findAll(spec,PageRequest.of(Math.max(0,page),15,Sort.by("stock").ascending().and(Sort.by("id").descending()))));return "admin/products";
 }
 @GetMapping("/products/new") public String newProduct(Model m){m.addAttribute("form",new ProductForm());return "admin/product-form";}
 @GetMapping("/products/{id}/edit") public String product(@PathVariable Long id,Model m){Product p=products.findById(id).orElseThrow();ProductForm f=new ProductForm();BeanUtils.copyProperties(p,f);f.setCategoryId(p.getCategory().getId());m.addAttribute("form",f);return "admin/product-form";}
 @PostMapping("/products/save") public String saveProduct(@Valid @ModelAttribute("form") ProductForm f,BindingResult errors,@RequestParam(required=false) MultipartFile image,@RequestParam(required=false) MultipartFile video,Principal principal){if(errors.hasErrors())return "admin/product-form";try{admin.product(f,image,video,principal.getName());}catch(BusinessException e){errors.reject("product",e.getMessage());return "admin/product-form";}return "redirect:/admin/products";}
 @PostMapping("/products/{id}/delete") public String deleteProduct(@PathVariable Long id){admin.archiveProduct(id);return "redirect:/admin/products";}
 @PostMapping("/products/{id}/stock") public String stock(@PathVariable Long id,@RequestParam int delta,@RequestParam(defaultValue="Điều chỉnh thủ công") String reason,Principal principal){admin.stock(id,delta,reason,principal.getName());return "redirect:/admin/products";}
 @GetMapping("/products/{id}/extras") public String extras(@PathVariable Long id,Model m){m.addAttribute("product",products.findById(id).orElseThrow());m.addAttribute("options",options.findByProductIdOrderByIdAsc(id));return "admin/extras";}
 @PostMapping("/products/{id}/gallery") public String gallery(@PathVariable Long id,@RequestParam java.util.List<MultipartFile> photos){admin.gallery(id,photos);return "redirect:/admin/products/"+id+"/extras";}
 @PostMapping("/products/{id}/gallery/remove") public String removeGallery(@PathVariable Long id,@RequestParam int index){admin.removeGallery(id,index);return "redirect:/admin/products/"+id+"/extras";}
 @PostMapping("/products/{id}/options") public String option(@PathVariable Long id,@Valid @ModelAttribute OptionForm form,BindingResult errors){if(errors.hasErrors())throw new BusinessException("Kiểm tra kích thước, hương vị và giá thêm.");optionService.add(id,form);return "redirect:/admin/products/"+id+"/extras";}
 @PostMapping("/products/{id}/options/{optionId}/disable") public String disable(@PathVariable Long id,@PathVariable Long optionId){optionService.disable(id,optionId);return "redirect:/admin/products/"+id+"/extras";}
 @GetMapping("/stock-history") public String history(@RequestParam(required=false) Long productId,@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("page",movements.history(productId,PageRequest.of(Math.max(page,0),20)));m.addAttribute("productId",productId);return "admin/stock-history";}
 @GetMapping("/categories") public String categories(Model m){m.addAttribute("form",new CategoryForm());return "admin/categories";}
 @GetMapping("/categories/{id}/edit") public String category(@PathVariable Long id,Model m){CategoryForm f=new CategoryForm();BeanUtils.copyProperties(categories.findById(id).orElseThrow(),f);m.addAttribute("form",f);return "admin/categories";}
 @PostMapping("/categories/save") public String saveCategory(@Valid @ModelAttribute("form") CategoryForm f,BindingResult errors){if(errors.hasErrors())return "admin/categories";admin.category(f);return "redirect:/admin/categories";}
 @PostMapping("/categories/{id}/delete") public String deleteCategory(@PathVariable Long id){admin.deleteCategory(id);return "redirect:/admin/categories";}
 @GetMapping("/vouchers") public String vouchers(@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("page",vouchers.findAll(PageRequest.of(Math.max(0,page),15,Sort.by("id").descending())));return "admin/vouchers";}
 @GetMapping("/vouchers/new") public String newVoucher(Model m){m.addAttribute("form",new VoucherForm());return "admin/voucher-form";}
 @GetMapping("/vouchers/{id}/edit") public String voucher(@PathVariable Long id,Model m){VoucherForm f=new VoucherForm();BeanUtils.copyProperties(vouchers.findById(id).orElseThrow(),f);m.addAttribute("form",f);return "admin/voucher-form";}
 @PostMapping("/vouchers/save") public String saveVoucher(@Valid @ModelAttribute("form") VoucherForm f,BindingResult errors){if(errors.hasErrors())return "admin/voucher-form";try{admin.voucher(f);}catch(BusinessException e){errors.reject("voucher",e.getMessage());return "admin/voucher-form";}return "redirect:/admin/vouchers";}
 @PostMapping("/vouchers/{id}/delete") public String deleteVoucher(@PathVariable Long id){admin.deleteVoucher(id);return "redirect:/admin/vouchers";}
 @GetMapping("/orders") public String orders(@RequestParam(defaultValue="0") int page,@RequestParam(required=false) OrderStatus status,Model m){var pageable=PageRequest.of(Math.max(0,page),15,Sort.by("createdAt").descending());m.addAttribute("status",status);m.addAttribute("statuses",OrderStatus.values());m.addAttribute("page",status==null?orders.findAll(pageable):orders.findByStatus(status,pageable));return "admin/orders";}
 @GetMapping("/orders/{id}") public String order(@PathVariable Long id,Model m){m.addAttribute("order",orders.findById(id).orElseThrow());m.addAttribute("drivers",deliveries.drivers());m.addAttribute("statuses",OrderStatus.values());m.addAttribute("paymentRecords",payments.orderHistory(id));m.addAttribute("paymentToken",java.util.UUID.randomUUID().toString());return "admin/order";}
 @PostMapping("/orders/{id}/status") public String status(@PathVariable Long id,@RequestParam OrderStatus status,Principal principal,org.springframework.web.servlet.mvc.support.RedirectAttributes flash){try{orderService.transition(id,status,principal.getName());flash.addFlashAttribute("success","Đã cập nhật trạng thái và thông báo cho khách.");}catch(BusinessException e){flash.addFlashAttribute("warning",e.getMessage());}return "redirect:/admin/orders/"+id;}
 @GetMapping("/users") public String users(@RequestParam(defaultValue="0") int page,Model m){m.addAttribute("page",users.findAll(PageRequest.of(Math.max(0,page),15,Sort.by("id").descending())));return "admin/users";}
 @PostMapping("/users/{id}/enabled") public String enabled(@PathVariable Long id,@RequestParam boolean enabled,Principal p){accounts.setEnabled(id,enabled,p.getName());return "redirect:/admin/users";}
}
