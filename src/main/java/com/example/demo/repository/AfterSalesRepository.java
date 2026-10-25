package com.example.demo.repository;
import com.example.demo.entity.AfterSalesRequest;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import java.util.*;
public interface AfterSalesRepository extends JpaRepository<AfterSalesRequest,Long> {
 Optional<AfterSalesRequest> findByOrderId(Long id);
 Optional<AfterSalesRequest> findByCustomCakeId(Long id);
 Optional<AfterSalesRequest> findByIdAndUserId(Long id,Long userId);
 Page<AfterSalesRequest> findByUserIdOrderByCreatedAtDesc(Long id,Pageable page);
 Page<AfterSalesRequest> findByStatus(AfterSalesRequest.Status status,Pageable page);
 long countByStatus(AfterSalesRequest.Status status);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select r from AfterSalesRequest r where r.id=:id") Optional<AfterSalesRequest> lockById(Long id);
}
