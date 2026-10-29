package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
public interface CustomCakeRequestRepository extends JpaRepository<CustomCakeRequest,Long>{
 Page<CustomCakeRequest> findByStatusInAndPaymentPaidGreaterThan(java.util.Collection<CustomCakeStatus> statuses,java.math.BigDecimal amount,Pageable page);
 long countByStatusInAndPaymentPaidGreaterThan(java.util.Collection<CustomCakeStatus> statuses,java.math.BigDecimal amount);

 java.util.List<CustomCakeRequest> findByCompletedAtGreaterThanEqualAndCompletedAtLessThan(java.time.LocalDateTime from,java.time.LocalDateTime to);
 java.util.List<CustomCakeRequest> findByReceiveDateBetweenOrderByReceiveDateAscReceiveSlotAsc(java.time.LocalDate from,java.time.LocalDate to);
 Page<CustomCakeRequest> findByUserIdOrderByCreatedAtDesc(Long userId,Pageable page);
 Page<CustomCakeRequest> findByStatus(CustomCakeStatus status,Pageable page);
 java.util.List<CustomCakeRequest> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(java.time.LocalDateTime start,java.time.LocalDateTime end);
 @Query("select coalesce(sum(r.quotedTotal-r.payment.refunded),0) from CustomCakeRequest r where r.status=com.example.demo.entity.CustomCakeStatus.COMPLETED") java.math.BigDecimal revenue();
 java.util.Optional<CustomCakeRequest> findByTokenAndUserId(String token,Long userId);
 java.util.Optional<CustomCakeRequest> findByIdAndUserId(Long id,Long userId);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select r from CustomCakeRequest r where r.id=:id") java.util.Optional<CustomCakeRequest> lockById(Long id);
}
