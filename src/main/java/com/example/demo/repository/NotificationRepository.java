package com.example.demo.repository;
import com.example.demo.entity.Notification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
public interface NotificationRepository extends JpaRepository<Notification,Long>{
 long countByUserIdAndReadFalse(Long userId);
 Page<Notification> findByUserIdOrderByCreatedAtDescIdDesc(Long userId,Pageable page);
 java.util.Optional<Notification> findByIdAndUserId(Long id,Long userId);
 @Modifying @Query("update Notification n set n.read=true where n.user.id=:userId and n.read=false") int readAll(Long userId);
}
