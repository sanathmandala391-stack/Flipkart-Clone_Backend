// ReturnRequestRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findByUserId(Long userId);
    List<ReturnRequest> findByStatus(ReturnRequest.ReturnStatus status);
    Optional<ReturnRequest> findByOrderItemId(Long orderItemId);
}