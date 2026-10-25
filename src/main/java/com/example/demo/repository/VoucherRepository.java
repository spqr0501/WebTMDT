package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
public interface VoucherRepository extends JpaRepository<Voucher,Long> { java.util.Optional<Voucher> findByCode(String code);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select v from Voucher v where v.id=:id") java.util.Optional<Voucher> lockById(@Param("id") Long id);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select v from Voucher v where v.code=:code") java.util.Optional<Voucher> lockByCode(@Param("code") String code); @Query("select v from Voucher v where v.active=true and v.startsAt<=:now and v.endsAt>:now and v.usedCount<v.usageLimit order by v.endsAt asc") java.util.List<Voucher> currentOffers(java.time.LocalDateTime now,Pageable page);
}
