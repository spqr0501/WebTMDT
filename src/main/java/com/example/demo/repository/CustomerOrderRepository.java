package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder,Long> { Page<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
 java.util.List<CustomerOrder> findByCompletedAtGreaterThanEqualAndCompletedAtLessThan(java.time.LocalDateTime from,java.time.LocalDateTime to);
 java.util.List<CustomerOrder> findByDeliveryDateBetweenOrderByDeliveryDateAscDeliverySlotAsc(java.time.LocalDate from,java.time.LocalDate to);
 java.util.Optional<CustomerOrder> findByIdAndUserId(Long id,Long userId);
 java.util.Optional<CustomerOrder> findByCheckoutTokenAndUserId(String token,Long userId);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select o from CustomerOrder o where o.id=:id") java.util.Optional<CustomerOrder> lockById(@Param("id") Long id);
 long countByStatus(OrderStatus status);
 @Query("select coalesce(sum(o.total-o.payment.refunded),0) from CustomerOrder o where o.status=com.example.demo.entity.OrderStatus.COMPLETED") java.math.BigDecimal revenue(); Page<CustomerOrder> findByStatus(OrderStatus status,Pageable page);
 java.util.List<CustomerOrder> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(java.time.LocalDateTime start,java.time.LocalDateTime end);
}
