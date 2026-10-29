package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder,Long> {
 Page<CustomerOrder> findByStatusAndPaymentPaidGreaterThan(OrderStatus status,java.math.BigDecimal amount,Pageable page);
 long countByStatusAndPaymentPaidGreaterThan(OrderStatus status,java.math.BigDecimal amount);
 Page<CustomerOrder> findByShipperIdAndStatusInOrderByDeliveryDateAscIdAsc(Long shipperId,java.util.Collection<OrderStatus> statuses,Pageable page);
 boolean existsByShipperIdAndStatusIn(Long shipperId,java.util.Collection<OrderStatus> statuses);
 Page<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
 java.util.List<CustomerOrder> findByCompletedAtGreaterThanEqualAndCompletedAtLessThan(java.time.LocalDateTime from,java.time.LocalDateTime to);
 java.util.List<CustomerOrder> findByDeliveryDateBetweenOrderByDeliveryDateAscDeliverySlotAsc(java.time.LocalDate from,java.time.LocalDate to);
 java.util.Optional<CustomerOrder> findByIdAndUserId(Long id,Long userId);
 java.util.Optional<CustomerOrder> findByCheckoutTokenAndUserId(String token,Long userId);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select o from CustomerOrder o where o.id=:id") java.util.Optional<CustomerOrder> lockById(@Param("id") Long id);
 long countByUserId(Long userId);
 long countByStatus(OrderStatus status);
 @Query("select coalesce(sum(o.total-o.payment.refunded),0) from CustomerOrder o where o.status=com.example.demo.entity.OrderStatus.COMPLETED") java.math.BigDecimal revenue(); Page<CustomerOrder> findByStatus(OrderStatus status,Pageable page);
 java.util.List<CustomerOrder> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(java.time.LocalDateTime start,java.time.LocalDateTime end);
}
