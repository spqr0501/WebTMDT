package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
public interface UserRepository extends JpaRepository<User,Long> { Page<User> findByRoleOrderByIdDesc(Role role,Pageable page);
 java.util.Optional<User> findByEmailIgnoreCase(String email);
 @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE) @Query("select u from User u where u.id=:id") java.util.Optional<User> lockById(@Param("id") Long id); java.util.List<User> findByRoleAndEnabledTrue(Role role);
}
