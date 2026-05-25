// ProductAttributeRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    List<ProductAttribute> findByProductIdOrderByDisplayOrderAsc(Long productId);
}