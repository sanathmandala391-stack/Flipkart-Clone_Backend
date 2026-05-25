// ProductVariantRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
    List<ProductVariant> findByProductIdAndIsActiveTrue(Long productId);
    Optional<ProductVariant> findBySku(String sku);

    // Low stock alert — qty below threshold
    List<ProductVariant> findByStockQtyLessThanAndIsActiveTrue(Integer threshold);
}