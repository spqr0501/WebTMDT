package com.example.demo.repository;
import com.example.demo.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDateTime;
import java.util.*;
public interface PasswordResetRepository extends JpaRepository<PasswordResetToken,Long> {
 Optional<PasswordResetToken> findByHash(String hash);
 boolean existsByUserIdAndCreatedAtAfter(Long userId,LocalDateTime since);
 @Modifying @Query("update PasswordResetToken t set t.usedAt=:now where t.user.id=:id and t.usedAt is null") void invalidate(Long id,LocalDateTime now);
}
