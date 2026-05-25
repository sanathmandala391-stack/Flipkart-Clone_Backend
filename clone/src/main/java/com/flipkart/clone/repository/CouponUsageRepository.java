// CouponUsageRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    // Has this user already used this coupon?
    Boolean existsByCouponIdAndUserId(Long couponId, Long userId);
    Long countByCouponId(Long couponId);
}