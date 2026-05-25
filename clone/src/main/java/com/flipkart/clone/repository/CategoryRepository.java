// CategoryRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull();              // top-level categories
    List<Category> findByParentId(Long parentId);     // subcategories
    Optional<Category> findBySlug(String slug);
    List<Category> findByIsActiveTrue();
}