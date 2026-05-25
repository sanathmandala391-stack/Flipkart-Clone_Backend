// VendorPayoutRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.VendorPayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VendorPayoutRepository extends JpaRepository<VendorPayout, Long> {
    List<VendorPayout> findByVendorIdOrderByCreatedAtDesc(Long vendorId);
    List<VendorPayout> findByStatus(VendorPayout.PayoutStatus status);
}