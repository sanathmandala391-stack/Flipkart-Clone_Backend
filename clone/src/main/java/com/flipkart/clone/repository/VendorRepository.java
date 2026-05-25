// VendorRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByUserId(Long userId);
    Optional<Vendor> findByGstNumber(String gstNumber);
    List<Vendor> findByIsApprovedFalse();         // pending approval
    List<Vendor> findByIsApprovedTrue();          // approved vendors
}