package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
public interface CategoryRepository extends JpaRepository<Category,Long> { boolean existsByNameIgnoreCase(String name); }
