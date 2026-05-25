// OrderItemRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // All items a vendor needs to fulfill
    List<OrderItem> findByVendorId(Long vendorId);

    // Items by vendor + status — e.g. only CONFIRMED items
    List<OrderItem> findByVendorIdAndStatus(Long vendorId, OrderItem.ItemStatus status);

    // Check if user has purchased a product — for verified review badge
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
            "WHERE oi.order.user.id = :userId " +
            "AND oi.productVariant.product.id = :productId " +
            "AND oi.status = 'DELIVERED'")
    Boolean hasUserPurchasedProduct(@Param("userId") Long userId,
                                    @Param("productId") Long productId);
}