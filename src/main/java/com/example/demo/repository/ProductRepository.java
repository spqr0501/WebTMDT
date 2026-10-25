package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> { @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select p from Product p where p.id=:id") java.util.Optional<Product> lockById(@Param("id") Long id);
 boolean existsByNameIgnoreCaseAndCategoryId(String name,Long categoryId);
 java.util.List<Product> findTop6ByActiveTrueAndFeaturedTrueOrderByIdDesc();
 java.util.List<Product> findByActiveTrueAndStockLessThanOrderByStockAsc(int stock);
 boolean existsByCategoryId(Long id); java.util.List<Product> findTop6ByActiveTrueOrderByIdDesc();
 java.util.List<Product> findTop3ByActiveTrueAndCategoryIdAndIdNotOrderByIdDesc(Long categoryId,Long id);
}
