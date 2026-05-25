// ProductQuestionRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.ProductQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductQuestionRepository extends JpaRepository<ProductQuestion, Long> {
    List<ProductQuestion> findByProductIdAndIsApprovedTrue(Long productId);
    List<ProductQuestion> findByIsApprovedFalse();   // admin moderation queue
}