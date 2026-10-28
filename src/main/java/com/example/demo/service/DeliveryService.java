package com.example.demo.service;
import com.example.demo.entity.*;
import com.example.demo.dto.ShipperForm;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.*;
import java.time.*;
import java.util.*;

@Service
public class DeliveryService {
 private final UserRepository users;private final CustomerOrderRepository orders;private final AccountService accounts;private final NotificationService notices;private final OrderService orderService;private final PasswordEncoder encoder;
 private static final List<OrderStatus> ACTIVE=List.of(OrderStatus.CONFIRMED,OrderStatus.PREPARING,OrderStatus.SHIPPING);
 public DeliveryService(UserRepository users,CustomerOrderRepository orders,AccountService accounts,NotificationService notices,OrderService orderService,PasswordEncoder encoder){this.users=users;this.orders=orders;this.accounts=accounts;this.notices=notices;this.orderService=orderService;this.encoder=encoder;}
 public List<User> drivers(){return users.findByRoleAndEnabledTrue(Role.SHIPPER);}
 public Page<User> allDrivers(int page){return users.findByRoleOrderByIdDesc(Role.SHIPPER,PageRequest.of(Math.max(0,page),20));}
 public User driver(Long id){return users.findById(id).filter(u->u.getRole()==Role.SHIPPER).orElseThrow(()->new BusinessException("Không tìm thấy shipper."));}
 private User actor(String email,Role role){var u=accounts.current(email);if(u.getRole()!=role)throw new BusinessException("Bạn không có quyền thực hiện thao tác này.");return u;}
 @Transactional public void save(ShipperForm f,String email){
  actor(email,Role.ADMIN);User u=f.getId()==null?new User():users.lockById(f.getId()).filter(x->x.getRole()==Role.SHIPPER).orElseThrow(()->new BusinessException("Không tìm thấy shipper."));
  String address=f.getEmail().strip().toLowerCase(Locale.ROOT);users.findByEmailIgnoreCase(address).filter(x->!Objects.equals(x.getId(),u.getId())).ifPresent(x->{throw new BusinessException("Email đã được sử dụng.");});
  if(!f.getEnabled()&&u.getId()!=null&&orders.existsByShipperIdAndStatusIn(u.getId(),ACTIVE))throw new BusinessException("Shipper còn đơn đang phụ trách. Hãy phân công lại trước khi ngừng hoạt động.");
  if(f.getId()==null&&(f.getPassword()==null||f.getPassword().isBlank()))throw new BusinessException("Nhập mật khẩu cho shipper mới.");
  if(f.getPassword()!=null&&!f.getPassword().isBlank()){
   if(f.getPassword().length()<8||f.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new BusinessException("Mật khẩu từ 8 ký tự, tối đa 72 byte.");u.setPassword(encoder.encode(f.getPassword()));
  }
  u.setRole(Role.SHIPPER);u.setName(f.getName().strip());u.setEmail(address);u.setShipperPhone(f.getPhone().strip());u.setShipperVehicle(f.getVehicle().strip());u.setEnabled(f.getEnabled());u.setAuthVersion(Math.addExact(u.getAuthVersion(),1));users.save(u);
 }
 @Transactional public void assign(Long id,Long driverId,String email){
  actor(email,Role.ADMIN);var o=orders.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn."));
  if(o.getFulfillmentMethod()!=FulfillmentMethod.DELIVERY||!ACTIVE.contains(o.getStatus())||o.getHandedOverAt()!=null)throw new BusinessException("Chỉ phân công đơn giao tận nơi đã xác nhận và chưa bàn giao.");
  User next=driverId==null?null:users.findById(driverId).filter(u->u.getRole()==Role.SHIPPER&&u.getEnabled()).orElseThrow(()->new BusinessException("Shipper không hoạt động."));
  if(next==null&&o.getStatus()==OrderStatus.SHIPPING)throw new BusinessException("Đơn đang giao cần được chuyển trực tiếp cho shipper khác.");
  if(Objects.equals(o.getShipper()==null?null:o.getShipper().getId(),driverId))return;
  if(o.getShipper()!=null)notices.send(o.getShipper(),"Đổi phân công đơn #"+id,"Bạn không còn phụ trách đơn này.","/shipper");
  o.setShipper(next);clearLocation(o);o.setDeliveryIssue(null);
  if(next!=null){notices.send(next,"Đơn giao mới #"+id,"Bạn được phân công giao bánh. Xem địa chỉ và lịch trong trang giao hàng.","/shipper/orders/"+id);notices.send(o.getUser(),"Shipper đơn #"+id,"Cửa hàng đã phân công "+next.getName()+" giao bánh. Có thể theo dõi khi đơn đang giao.","/orders/"+id+"/tracking");}
 }
 public Page<CustomerOrder> jobs(String email,int page){return orders.findByShipperIdAndStatusInOrderByDeliveryDateAscIdAsc(actor(email,Role.SHIPPER).getId(),ACTIVE,PageRequest.of(Math.max(0,page),15));}
 public CustomerOrder assigned(String email,Long id){var u=actor(email,Role.SHIPPER);var o=orders.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn."));requireAssigned(o,u);return o;}
 private void requireAssigned(CustomerOrder o,User u){if(o.getShipper()==null||!o.getShipper().getId().equals(u.getId())||o.getFulfillmentMethod()!=FulfillmentMethod.DELIVERY)throw new BusinessException("Bạn không được phân công đơn này.");}
 private CustomerOrder locked(String email,Long id){var u=actor(email,Role.SHIPPER);var o=orders.lockById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn."));requireAssigned(o,u);return o;}
 @Transactional public void start(String email,Long id){var o=locked(email,id);if(o.getStatus()!=OrderStatus.PREPARING)throw new BusinessException("Chỉ nhận giao khi đơn đang chuẩn bị và bánh đã sẵn sàng.");orderService.transition(id,OrderStatus.SHIPPING,email);}
 public static void clearLocation(CustomerOrder o){o.setTrackingKey(null);o.setLocationSharing(false);o.setShipperLatitude(null);o.setShipperLongitude(null);o.setLocationUpdatedAt(null);o.setLocationCapturedAt(null);o.setLocationAccuracy(null);}
 @Transactional public String begin(String email,Long id){var o=locked(email,id);if(o.getStatus()!=OrderStatus.SHIPPING||o.getHandedOverAt()!=null)throw new BusinessException("Đơn không còn trong quá trình giao.");clearLocation(o);String key=UUID.randomUUID().toString();o.setTrackingKey(key);return key;}
 @Transactional public void location(String email,Long id,String key,Double lat,Double lng,Double accuracy,Instant captured){
  var o=locked(email,id);if(o.getStatus()!=OrderStatus.SHIPPING||o.getHandedOverAt()!=null)throw new BusinessException("Đơn không còn trong quá trình giao.");if(key==null||!key.equals(o.getTrackingKey()))throw new BusinessException("Phiên chia sẻ đã dừng hoặc được thay thế. Hãy bật chia sẻ lại.");GeoService.validate(lat,lng);
  if(lat==null||accuracy==null||!Double.isFinite(accuracy)||accuracy<0||accuracy>10000||captured==null||captured.isBefore(Instant.now().minusSeconds(120))||captured.isAfter(Instant.now().plusSeconds(30)))throw new BusinessException("GPS quá cũ hoặc chưa hợp lệ. Vui lòng lấy lại vị trí.");
  if(o.getLocationCapturedAt()!=null&&!captured.isAfter(o.getLocationCapturedAt()))return;
  if(o.getLocationUpdatedAt()!=null&&o.getLocationUpdatedAt().isAfter(Instant.now().minusSeconds(5)))return;
  o.setLocationSharing(true);o.setShipperLatitude(lat);o.setShipperLongitude(lng);o.setLocationAccuracy(accuracy);o.setLocationCapturedAt(captured);o.setLocationUpdatedAt(Instant.now());
 }
 @Transactional public void stop(String email,Long id){var o=locked(email,id);clearLocation(o);}
 @Transactional public void delivered(String email,Long id){var o=locked(email,id);if(o.getStatus()!=OrderStatus.SHIPPING)throw new BusinessException("Đơn chưa ở trạng thái đang giao.");if(o.getHandedOverAt()!=null)return;o.setHandedOverAt(Instant.now());clearLocation(o);notices.send(o.getUser(),"Đã bàn giao bánh #"+id,"Shipper đã báo giao bánh thành công. Nếu chưa nhận được, vui lòng liên hệ cửa hàng ngay.","/orders/"+id);notices.admins("Shipper báo đã giao #"+id,"Kiểm tra bàn giao và đối soát tiền thực tế trước khi hoàn tất đơn.","/admin/orders/"+id);}
 @Transactional public void issue(String email,Long id,String message){var o=locked(email,id);if(o.getStatus()!=OrderStatus.SHIPPING||o.getHandedOverAt()!=null||message==null||message.isBlank()||message.length()>500)throw new BusinessException("Nhập sự cố giao hàng tối đa 500 ký tự cho đơn đang giao.");o.setDeliveryIssue(message.strip());clearLocation(o);notices.admins("Sự cố giao đơn #"+id,message.strip(),"/admin/orders/"+id);notices.send(o.getUser(),"Đơn #"+id+" cần hỗ trợ giao hàng","Shipper đã báo sự cố. Cửa hàng sẽ liên hệ để thống nhất phương án giao.","/orders/"+id);}
 public record Tracking(Long orderId,String status,String driverName,String driverPhone,String vehicle,Double destinationLatitude,Double destinationLongitude,boolean sharing,boolean stale,Double latitude,Double longitude,Double accuracy,Instant updatedAt,boolean handedOver,boolean issue){}
 @Transactional(readOnly=true) public Tracking tracking(String email,Long id){
  User u=accounts.current(email);var o=orders.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy đơn."));
  if(u.getRole()!=Role.ADMIN&&!o.getUser().getId().equals(u.getId())){if(u.getRole()!=Role.SHIPPER)throw new BusinessException("Không tìm thấy đơn của bạn.");requireAssigned(o,u);}
  if(o.getFulfillmentMethod()!=FulfillmentMethod.DELIVERY)throw new BusinessException("Đơn nhận tại cửa hàng không có shipper.");
  boolean active=o.getStatus()==OrderStatus.SHIPPING&&o.getHandedOverAt()==null;var d=o.getShipper();boolean visible=active&&o.getLocationSharing()&&d!=null&&d.getEnabled()&&o.getShipperLatitude()!=null;
  boolean stale=visible&&(o.getLocationUpdatedAt()==null||o.getLocationUpdatedAt().isBefore(Instant.now().minusSeconds(90)));
  return new Tracking(id,o.getStatus().getLabel(),d==null?null:d.getName(),d==null?null:d.getShipperPhone(),d==null?null:d.getShipperVehicle(),o.getDeliveryLatitude(),o.getDeliveryLongitude(),visible,stale,visible?o.getShipperLatitude():null,visible?o.getShipperLongitude():null,visible?o.getLocationAccuracy():null,visible?o.getLocationUpdatedAt():null,o.getHandedOverAt()!=null,o.getDeliveryIssue()!=null);
 }
}
