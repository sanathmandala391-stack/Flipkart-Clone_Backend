package com.flipkart.clone.controller;

import com.flipkart.clone.entity.Category;
import com.flipkart.clone.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // PUBLIC
    @GetMapping
    public ResponseEntity<List<Category>> getTopLevel() {
        return ResponseEntity.ok(
                categoryService.getTopLevelCategories());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(
                categoryService.getAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                categoryService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Category> getBySlug(
            @PathVariable String slug) {
        return ResponseEntity.ok(
                categoryService.getBySlug(slug));
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<Category>> getSubs(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                categoryService.getSubCategories(id));
    }

    // ADMIN ONLY
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> create(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(
                categoryService.createCategory(body));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(
                categoryService.updateCategory(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
                Map.of("message", "Category deactivated"));
    }
}