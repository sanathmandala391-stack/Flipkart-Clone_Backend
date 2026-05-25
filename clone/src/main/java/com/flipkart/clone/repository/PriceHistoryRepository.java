// PriceHistoryRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    // Last 30 price changes for the price-history chart
    List<PriceHistory> findTop30ByProductVariantIdOrderByChangedAtDesc(Long variantId);
}