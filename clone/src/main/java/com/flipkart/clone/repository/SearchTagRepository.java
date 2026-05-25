// SearchTagRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.SearchTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SearchTagRepository extends JpaRepository<SearchTag, Long> {
    List<SearchTag> findByProductId(Long productId);
    List<SearchTag> findByTagContainingIgnoreCase(String keyword);
}