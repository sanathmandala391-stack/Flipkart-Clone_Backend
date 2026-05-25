package com.flipkart.clone.service;

import com.flipkart.clone.entity.Category;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getTopLevelCategories() {
        return categoryRepository.findByParentIsNull();
    }

    public List<Category> getAllActive() {
        return categoryRepository.findByIsActiveTrue();
    }

    public List<Category> getSubCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + id));
    }

    public Category getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found: " + slug));
    }

    @Transactional
    public Category createCategory(Map<String, Object> body) {

        String slug = body.get("slug").toString();
        if (categoryRepository.findBySlug(slug).isPresent()) {
            throw new BadRequestException(
                    "Slug already exists: " + slug);
        }

        Category parent = null;
        if (body.containsKey("parentId")
                && body.get("parentId") != null) {
            Long parentId = Long.valueOf(
                    body.get("parentId").toString());
            parent = categoryRepository.findById(parentId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Parent category not found"));
        }

        Category category = Category.builder()
                .name(body.get("name").toString())
                .slug(slug)
                .parent(parent)
                .imageUrl(body.containsKey("imageUrl")
                        ? body.get("imageUrl").toString() : null)
                .isActive(true)
                .sortOrder(body.containsKey("sortOrder")
                        ? Integer.valueOf(
                        body.get("sortOrder").toString()) : 0)
                .build();

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id,
                                   Map<String, Object> body) {
        Category category = getById(id);

        if (body.containsKey("name"))
            category.setName(body.get("name").toString());
        if (body.containsKey("imageUrl"))
            category.setImageUrl(
                    body.get("imageUrl").toString());
        if (body.containsKey("isActive"))
            category.setIsActive(
                    (Boolean) body.get("isActive"));
        if (body.containsKey("sortOrder"))
            category.setSortOrder(Integer.valueOf(
                    body.get("sortOrder").toString()));

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getById(id);
        category.setIsActive(false);
        categoryRepository.save(category);
    }
}