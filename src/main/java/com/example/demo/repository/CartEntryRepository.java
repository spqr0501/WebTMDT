package com.example.demo.repository;
import com.example.demo.entity.CartEntry;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface CartEntryRepository extends JpaRepository<CartEntry,Long>{
 java.util.List<CartEntry> findByUserId(Long userId);
 @Modifying @Query("delete from CartEntry e where e.user.id=:id") void deleteForUser(@Param("id") Long id);
}
