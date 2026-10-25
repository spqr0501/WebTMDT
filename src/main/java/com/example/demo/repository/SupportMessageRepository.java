package com.example.demo.repository;
import com.example.demo.entity.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
public interface SupportMessageRepository extends JpaRepository<SupportMessage,Long> {
 Page<SupportMessage> findByTicketIdOrderByIdAsc(Long ticketId,Pageable page);
}
