// ProductRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // All active products for a vendor
    List<Product> findByVendorIdAndIsActiveTrue(Long vendorId);

    // All active products in a category
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    // Featured products for homepage
    List<Product> findByIsFeaturedTrueAndIsActiveTrue();

    // Search by name or brand — powers search bar
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Filter by category + brand
    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brand IS NULL OR LOWER(p.brand) = LOWER(:brand))")
    Page<Product> filterProducts(@Param("categoryId") Long categoryId,
                                 @Param("brand") String brand,
                                 Pageable pageable);

    // Count products per vendor
    Long countByVendorId(Long vendorId);
}