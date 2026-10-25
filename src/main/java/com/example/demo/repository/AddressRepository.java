package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
public interface AddressRepository extends JpaRepository<Address,Long> { java.util.List<Address> findByUserIdOrderByIdDesc(Long userId); java.util.Optional<Address> findByIdAndUserId(Long id, Long userId); }
