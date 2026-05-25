// CartItemRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndProductVariantId(Long userId, Long variantId);
    void deleteByUserId(Long userId);            // clear cart after order placed
    Long countByUserId(Long userId);             // cart item count for badge
}