package com.example.demo.repository;
import com.example.demo.entity.StripePayment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.util.*;
public interface StripePaymentRepository extends JpaRepository<StripePayment,String> {
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select p from StripePayment p where p.id=:id") Optional<StripePayment> lockById(@Param("id") String id);
 List<StripePayment> findByOrderIdOrderByCreatedAtDesc(Long id);
 List<StripePayment> findByCustomCakeIdOrderByCreatedAtDesc(Long id);
 Page<StripePayment> findAllByOrderByCreatedAtDesc(Pageable p);
}
