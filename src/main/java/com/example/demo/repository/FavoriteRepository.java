package com.example.demo.repository;
import com.example.demo.entity.Favorite;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import java.util.*;
public interface FavoriteRepository extends JpaRepository<Favorite,Long> {
 Optional<Favorite> findByUserIdAndProductId(Long userId,Long productId);
 Page<Favorite> findByUserIdAndProductActiveTrueOrderByCreatedAtDescIdDesc(Long id,Pageable page);
 @Query("select f.product.id from Favorite f where f.user.id=:id") Set<Long> productIds(Long id);
}
