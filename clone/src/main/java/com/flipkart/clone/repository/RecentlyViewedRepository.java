// RecentlyViewedRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.RecentlyViewed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewed, Long> {
    // Newest 20 for homepage section
    List<RecentlyViewed> findTop20ByUserIdOrderByViewedAtDesc(Long userId);
    Optional<RecentlyViewed> findByUserIdAndProductId(Long userId, Long productId);
    Long countByUserId(Long userId);
}