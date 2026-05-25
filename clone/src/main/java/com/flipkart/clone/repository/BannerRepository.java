// BannerRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveTrueOrderBySortOrderAsc();
    List<Banner> findByPositionAndIsActiveTrue(Banner.BannerPosition position);
}