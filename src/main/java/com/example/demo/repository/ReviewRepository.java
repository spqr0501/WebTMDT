package com.example.demo.repository;
import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import java.util.*;
public interface ReviewRepository extends JpaRepository<Review,Long> {
 boolean existsByProductIdAndUserId(Long productId,Long userId);
 @Query("select r from Review r where r.product.id=:id and r.visible=true and (:rating is null or r.rating=:rating) order by r.createdAt desc")
 Page<Review> published(Long id,Integer rating,Pageable pageable);
 @Query("select r.rating,count(r) from Review r where r.product.id=:id and r.visible=true group by r.rating")
 List<Object[]> distribution(Long id);
}
