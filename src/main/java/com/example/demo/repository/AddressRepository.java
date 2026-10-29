package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
public interface AddressRepository extends JpaRepository<Address,Long> {
 @org.springframework.data.jpa.repository.Modifying @org.springframework.data.jpa.repository.Query("update Address a set a.defaultAddress=false where a.user.id=:userId") void clearDefaults(@org.springframework.data.repository.query.Param("userId") Long userId);
 java.util.List<Address> findByUserIdOrderByIdDesc(Long userId); java.util.Optional<Address> findByIdAndUserId(Long id, Long userId); }
