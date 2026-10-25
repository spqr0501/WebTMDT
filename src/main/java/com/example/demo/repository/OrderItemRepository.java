package com.example.demo.repository;
import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
 @Query("select i.productName, sum(i.quantity) from OrderItem i where i.order.status=com.example.demo.entity.OrderStatus.COMPLETED group by i.productName order by sum(i.quantity) desc") java.util.List<Object[]> bestSellers(Pageable page);
 @Query("select count(i) from OrderItem i where i.order.user.id=:userId and i.product.id=:productId and i.order.status=com.example.demo.entity.OrderStatus.COMPLETED") long completedPurchases(Long userId,Long productId);
 @Query("select i.product.id from OrderItem i where i.order.status=com.example.demo.entity.OrderStatus.COMPLETED and i.product.active=true group by i.product.id order by sum(i.quantity) desc") java.util.List<Long> popularIds(Pageable page);
}
