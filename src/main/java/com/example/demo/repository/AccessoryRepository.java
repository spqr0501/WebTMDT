package com.example.demo.repository;
import com.example.demo.entity.Accessory;
import org.springframework.data.jpa.repository.*;
public interface AccessoryRepository extends JpaRepository<Accessory,Long>{
 boolean existsByNameIgnoreCase(String name);
 java.util.List<Accessory> findByActiveTrueOrderByIdAsc();
 long countByActiveTrue();
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select a from Accessory a where a.id=:id") java.util.Optional<Accessory> lockById(Long id);
}
