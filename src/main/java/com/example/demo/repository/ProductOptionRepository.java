package com.example.demo.repository;
import com.example.demo.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ProductOptionRepository extends JpaRepository<ProductOption,Long> {
 List<ProductOption> findByProductIdOrderByIdAsc(Long id);
 List<ProductOption> findByProductIdAndActiveTrueOrderByExtraPriceAsc(Long id);
 Optional<ProductOption> findByIdAndProductIdAndActiveTrue(Long id,Long productId);
 boolean existsByProductIdAndActiveTrue(Long id);
}
