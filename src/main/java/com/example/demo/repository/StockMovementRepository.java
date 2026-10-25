package com.example.demo.repository;
import com.example.demo.entity.StockMovement;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
public interface StockMovementRepository extends JpaRepository<StockMovement,Long> {
 @Query("select s from StockMovement s where (:productId is null or s.product.id=:productId) order by s.createdAt desc,s.id desc")
 Page<StockMovement> history(Long productId,Pageable page);
}
