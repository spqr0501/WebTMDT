package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage,Long> { long countByVoucherIdAndUserId(Long voucherId,Long userId); }
