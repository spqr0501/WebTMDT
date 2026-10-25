package com.example.demo.repository;
import com.example.demo.entity.CakeDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CakeDesignRepository extends JpaRepository<CakeDesign,Long> {
 List<CakeDesign> findByRequestIdOrderByIdDesc(Long requestId);
 Optional<CakeDesign> findTopByRequestIdOrderByIdDesc(Long requestId);
}
