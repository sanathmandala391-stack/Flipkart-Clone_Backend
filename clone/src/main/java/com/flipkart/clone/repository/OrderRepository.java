// OrderRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
//
//@Repository
//public interface OrderRepository extends JpaRepository<Order, Long> {
//    // User's order history — newest first
//    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
//
//    // All orders for admin
//    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
//
//    // Orders by status — for vendor dashboard
//    List<Order> findByStatus(Order.OrderStatus status);
//
//    // Count orders per user
//    Long countByUserId(Long userId);
//}




//new //

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Order> findByStatus(Order.OrderStatus status);

    Long countByUserId(Long userId);

    // 🔥 ADD THIS
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}