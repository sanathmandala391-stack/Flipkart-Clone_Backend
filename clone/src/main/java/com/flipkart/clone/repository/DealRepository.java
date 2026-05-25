// DealRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    // Active deals right now — for homepage flash sale section
    @Query("SELECT d FROM Deal d WHERE d.isActive = true " +
            "AND d.startAt <= :now AND d.endAt >= :now")
    List<Deal> findActiveDeals(LocalDateTime now);

    List<Deal> findByDealTypeAndIsActiveTrue(Deal.DealType dealType);
}