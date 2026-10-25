package com.example.demo.repository;
import com.example.demo.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord,Long> {
 java.util.List<PaymentRecord> findByAfterSalesIdOrderByCreatedAtAscIdAsc(Long id);
 java.util.Optional<PaymentRecord> findByToken(String token);
 java.util.List<PaymentRecord> findByOrderIdOrderByCreatedAtAscIdAsc(Long id);
 java.util.List<PaymentRecord> findByCustomCakeIdOrderByCreatedAtAscIdAsc(Long id);
}
