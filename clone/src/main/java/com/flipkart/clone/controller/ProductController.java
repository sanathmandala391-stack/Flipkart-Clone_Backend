package com.flipkart.clone.controller;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ── GET /api/products?page=0&size=20&sort=newest ──────────────
    // PUBLIC — anyone can browse
    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "newest") String sort) {

        return ResponseEntity.ok(
                productService.getAllProducts(page, size, sort));
    }

    // ── GET /api/products/featured ────────────────────────────────
    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeatured() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    // ── GET /api/products/search?q=iphone ────────────────────────
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                productService.searchProducts(q, page, size));
    }

    // ── GET /api/products/filter?categoryId=1&brand=Apple ────────
    @GetMapping("/filter")
    public ResponseEntity<Page<Product>> filter(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(defaultValue = "0")      int page,
            @RequestParam(defaultValue = "20")     int size,
            @RequestParam(defaultValue = "newest") String sort) {

        return ResponseEntity.ok(
                productService.filterProducts(
                        categoryId, brand, page, size, sort));
    }

    // ── GET /api/products/category/{categoryId} ───────────────────
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                productService.getProductsByCategory(categoryId, page, size));
    }

    // ── GET /api/products/{id} ────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ── GET /api/products/{id}/variants ──────────────────────────
    @GetMapping("/{id}/variants")
    public ResponseEntity<List<ProductVariant>> getVariants(
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getVariants(id));
    }

    // ── GET /api/products/{id}/images ─────────────────────────────
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ProductImage>> getImages(
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getImages(id));
    }

    // ── GET /api/products/{id}/attributes ─────────────────────────
    @GetMapping("/{id}/attributes")
    public ResponseEntity<List<ProductAttribute>> getAttributes(
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getAttributes(id));
    }

    // ── GET /api/products/variants/{variantId}/price-history ──────
    @GetMapping("/variants/{variantId}/price-history")
    public ResponseEntity<List<PriceHistory>> getPriceHistory(
            @PathVariable Long variantId) {
        return ResponseEntity.ok(
                productService.getPriceHistory(variantId));
    }

    // ══════════════════════════════════════════════════════════════
    // VENDOR ONLY — requires ROLE_VENDOR
    // ══════════════════════════════════════════════════════════════

    // ── POST /api/products ────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Product> createProduct(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(
                productService.createProduct(userId, body));
    }

    // ── PUT /api/products/{id} ────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(
                productService.updateProduct(id, userId, body));
    }

    // ── DELETE /api/products/{id} ─────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Map<String, String>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        productService.deleteProduct(id, getUserId(userDetails));
        return ResponseEntity.ok(
                Map.of("message", "Product deactivated successfully"));
    }

    // ── POST /api/products/{id}/variants ──────────────────────────
    @PostMapping("/{id}/variants")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ProductVariant> addVariant(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(
                productService.addVariant(id, userId, body));
    }

    // ── PUT /api/products/variants/{variantId} ────────────────────
    @PutMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ProductVariant> updateVariant(
            @PathVariable Long variantId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(
                productService.updateVariant(variantId, userId, body));
    }

    // ── POST /api/products/{id}/images ────────────────────────────
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ProductImage> addImage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId    = getUserId(userDetails);
        String url     = body.get("url").toString();
        Boolean isPrimary = body.containsKey("isPrimary")
                ? (Boolean) body.get("isPrimary") : false;

        return ResponseEntity.ok(
                productService.addImage(id, userId, url, isPrimary));
    }

    // ── POST /api/products/{id}/attributes ────────────────────────
    @PostMapping("/{id}/attributes")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ProductAttribute> addAttribute(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserId(userDetails);
        String key  = body.get("key").toString();
        String val  = body.get("value").toString();

        return ResponseEntity.ok(
                productService.addAttribute(id, userId, key, val));
    }

    // ── GET /api/products/vendor/my-products ──────────────────────
    @GetMapping("/vendor/my-products")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<Product>> getMyProducts(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                productService.getVendorProducts(getUserId(userDetails)));
    }

    // ── GET /api/products/vendor/low-stock ────────────────────────
    @GetMapping("/vendor/low-stock")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<ProductVariant>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStockVariants());
    }

    // ══════════════════════════════════════════════════════════════
    // ADMIN ONLY
    // ══════════════════════════════════════════════════════════════

    // ── PUT /api/products/{id}/feature ────────────────────────────
    @PutMapping("/{id}/feature")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> toggleFeatured(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleFeatured(id));
    }

    // ── PUT /api/products/{id}/toggle-active ──────────────────────
    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleActive(id));
    }

    // ── Helper: get userId from JWT principal ─────────────────────
    private Long getUserId(UserDetails userDetails) {
        // email is the username in JWT
        // We need to get User from DB — inject UserRepository or
        // use a helper service. For now use AuthService pattern.
        // We'll fix this cleanly with a helper below.
        return ((com.flipkart.clone.security.CustomUserDetails)
                userDetails).getUserId();
    }
}