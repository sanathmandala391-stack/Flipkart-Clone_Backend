// CommissionRuleRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {
    Optional<CommissionRule> findByCategoryIdAndIsActiveTrue(Long categoryId);
}