package com.example.demo.repository;
import com.example.demo.entity.SupportTicket;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import java.util.*;
public interface SupportTicketRepository extends JpaRepository<SupportTicket,Long> {
 Page<SupportTicket> findByUserIdOrderByUpdatedAtDesc(Long userId,Pageable page);
 Page<SupportTicket> findByStatus(SupportTicket.Status status,Pageable page);
 Optional<SupportTicket> findByIdAndUserId(Long id,Long userId);
 Optional<SupportTicket> findByTokenAndUserId(String token,Long userId);
 long countByStatus(SupportTicket.Status status);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select t from SupportTicket t where t.id=:id") Optional<SupportTicket> lockById(Long id);
}
