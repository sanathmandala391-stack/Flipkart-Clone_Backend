package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ProductAttributeRepository attributeRepository;
    private final SearchTagRepository searchTagRepository;
    private final CategoryRepository categoryRepository;
    private final VendorRepository vendorRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ReviewRepository reviewRepository;

    // ── GET all products (paginated + sorted) ────────────────────
    public Page<Product> getAllProducts(int page, int size, String sortBy) {
        Sort sort = switch (sortBy) {
            case "price_low"  -> Sort.by("variants.price").ascending();
            case "price_high" -> Sort.by("variants.price").descending();
            case "rating"     -> Sort.by("avgRating").descending();
            case "newest"     -> Sort.by("createdAt").descending();
            default           -> Sort.by("isFeatured").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable);
    }

    // ── GET single product by ID ──────────────────────────────────
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));
    }

    // ── SEARCH products by keyword ────────────────────────────────
    public Page<Product> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchByKeyword(keyword, pageable);
    }

    // ── FILTER products ───────────────────────────────────────────
    public Page<Product> filterProducts(Long categoryId,
                                        String brand,
                                        int page, int size,
                                        String sortBy) {
        Sort sort = switch (sortBy) {
            case "rating"  -> Sort.by("avgRating").descending();
            case "newest"  -> Sort.by("createdAt").descending();
            default        -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.filterProducts(categoryId, brand, pageable);
    }

    // ── GET featured products for homepage ────────────────────────
    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue();
    }

    // ── GET products by category ──────────────────────────────────
    public Page<Product> getProductsByCategory(Long categoryId,
                                               int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return productRepository.findByCategoryIdAndIsActiveTrue(
                categoryId, pageable);
    }

    // ── CREATE product (vendor only) ──────────────────────────────
//    @Transactional
//    public Product createProduct(Long userId, Map<String, Object> body) {
//
//        Vendor vendor = vendorRepository.findByUserId(userId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Vendor profile not found"));
//
//        if (!vendor.getIsApproved()) {
//            throw new BadRequestException(
//                    "Your vendor account is pending approval");
//        }
//
//        Long categoryId = Long.valueOf(body.get("categoryId").toString());
//        Category category = categoryRepository.findById(categoryId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Category not found"));
//
//        // Build product entity directly — no DTO
//        Product product = Product.builder()
//                .vendor(vendor)
//                .category(category)
//                .name(body.get("name").toString())
//                .description(body.get("description").toString())
//                .brand(body.get("brand").toString())
//                .isActive(true)
//                .isFeatured(false)
//                .avgRating(0.0)
//                .totalReviews(0)
//                .build();
//
//        Product saved = productRepository.save(product);
//
//        // Save search tags if provided
//        if (body.containsKey("tags")) {
//            List<String> tags = (List<String>) body.get("tags");
//            tags.forEach(tag -> {
//                SearchTag searchTag = SearchTag.builder()
//                        .product(saved)
//                        .tag(tag.toLowerCase())
//                        .build();
//                searchTagRepository.save(searchTag);
//            });
//        }
//
//        return saved;
//    }


    @Transactional
    public Product createProduct(Long userId, Map<String, Object> body) {

        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        Category category = categoryRepository.findById(
                Long.valueOf(body.get("categoryId").toString())
        ).orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .vendor(vendor)
                .category(category)
                .name(body.get("name").toString())
                .description(body.get("description").toString())
                .brand(body.get("brand").toString())
                .isActive(true)
                .isFeatured(false)
                .avgRating(0.0)
                .totalReviews(0)
                .build();

        Product saved = productRepository.save(product);

        // ⭐ FIX: SAVE IMAGES HERE
        if (body.containsKey("images")) {

            List<String> images = (List<String>) body.get("images");

            int i = 0;
            for (String url : images) {

                ProductImage img = ProductImage.builder()
                        .product(saved)
                        .url(url)
                        .isPrimary(i == 0)
                        .sortOrder(i)
                        .build();

                imageRepository.save(img);
                i++;
            }
        }

        return saved;
    }

    // ── UPDATE product ────────────────────────────────────────────
    @Transactional
    public Product updateProduct(Long productId,
                                 Long userId,
                                 Map<String, Object> body) {

        Product product = getProductById(productId);

        // Make sure this vendor owns this product
        if (!product.getVendor().getUser().getId().equals(userId)) {
            throw new BadRequestException(
                    "You are not authorized to edit this product");
        }

        if (body.containsKey("name"))
            product.setName(body.get("name").toString());
        if (body.containsKey("description"))
            product.setDescription(body.get("description").toString());
        if (body.containsKey("brand"))
            product.setBrand(body.get("brand").toString());
        if (body.containsKey("isActive"))
            product.setIsActive((Boolean) body.get("isActive"));

        if (body.containsKey("categoryId")) {
            Long catId = Long.valueOf(body.get("categoryId").toString());
            Category category = categoryRepository.findById(catId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    // ── DELETE product (soft delete) ──────────────────────────────
    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        Product product = getProductById(productId);

        if (!product.getVendor().getUser().getId().equals(userId)) {
            throw new BadRequestException(
                    "You are not authorized to delete this product");
        }

        // Soft delete — just mark inactive
        product.setIsActive(false);
        productRepository.save(product);
    }

    // ══════════════════════════════════════════════════════════════
    // VARIANT OPERATIONS
    // ══════════════════════════════════════════════════════════════

    // ── ADD variant to product ────────────────────────────────────
    @Transactional
    public ProductVariant addVariant(Long productId,
                                     Long userId,
                                     Map<String, Object> body) {

        Product product = getProductById(productId);

        if (!product.getVendor().getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized");
        }

        BigDecimal price = new BigDecimal(body.get("price").toString());
        BigDecimal mrp   = new BigDecimal(body.get("mrp").toString());

        if (price.compareTo(mrp) > 0) {
            throw new BadRequestException(
                    "Selling price cannot be greater than MRP");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(body.getOrDefault("sku", "").toString())
                .price(price)
                .mrp(mrp)
                .stockQty(Integer.valueOf(
                        body.get("stockQty").toString()))
                .size(body.getOrDefault("size", "").toString())
                .color(body.getOrDefault("color", "").toString())
                .colorHex(body.getOrDefault("colorHex", "").toString())
                .isActive(true)
                .build();

        ProductVariant saved = variantRepository.save(variant);

        // Log initial price in price history
        PriceHistory priceHistory = PriceHistory.builder()
                .productVariant(saved)
                .price(price)
                .mrp(mrp)
                .build();
        priceHistoryRepository.save(priceHistory);

        return saved;
    }

    // ── UPDATE variant (price/stock) ──────────────────────────────
    @Transactional
    public ProductVariant updateVariant(Long variantId,
                                        Long userId,
                                        Map<String, Object> body) {

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Variant not found"));

        if (!variant.getProduct().getVendor()
                .getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized");
        }

        boolean priceChanged = false;
        BigDecimal newPrice  = variant.getPrice();
        BigDecimal newMrp    = variant.getMrp();

        if (body.containsKey("price")) {
            newPrice = new BigDecimal(body.get("price").toString());
            variant.setPrice(newPrice);
            priceChanged = true;
        }
        if (body.containsKey("mrp")) {
            newMrp = new BigDecimal(body.get("mrp").toString());
            variant.setMrp(newMrp);
            priceChanged = true;
        }
        if (body.containsKey("stockQty")) {
            variant.setStockQty(
                    Integer.valueOf(body.get("stockQty").toString()));
        }
        if (body.containsKey("isActive")) {
            variant.setIsActive((Boolean) body.get("isActive"));
        }

        ProductVariant saved = variantRepository.save(variant);

        // Log price change in history
        if (priceChanged) {
            PriceHistory history = PriceHistory.builder()
                    .productVariant(saved)
                    .price(newPrice)
                    .mrp(newMrp)
                    .build();
            priceHistoryRepository.save(history);
        }

        return saved;
    }

    // ── GET all variants for a product ────────────────────────────
    public List<ProductVariant> getVariants(Long productId) {
        return variantRepository.findByProductIdAndIsActiveTrue(productId);
    }

    // ── GET price history for a variant ──────────────────────────
    public List<PriceHistory> getPriceHistory(Long variantId) {
        return priceHistoryRepository
                .findTop30ByProductVariantIdOrderByChangedAtDesc(variantId);
    }

    // ══════════════════════════════════════════════════════════════
    // IMAGE OPERATIONS
    // ══════════════════════════════════════════════════════════════

    // ── ADD image to product ──────────────────────────────────────
    @Transactional
    public ProductImage addImage(Long productId,
                                 Long userId,
                                 String imageUrl,
                                 Boolean isPrimary) {

        Product product = getProductById(productId);

        if (!product.getVendor().getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized");
        }

        // If setting as primary — unset existing primary
        if (isPrimary) {
            imageRepository.findByProductIdAndIsPrimaryTrue(productId)
                    .ifPresent(img -> {
                        img.setIsPrimary(false);
                        imageRepository.save(img);
                    });
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .url(imageUrl)
                .isPrimary(isPrimary)
                .sortOrder(0)
                .build();

        return imageRepository.save(image);
    }

    // ── GET images for product ────────────────────────────────────
    public List<ProductImage> getImages(Long productId) {
        return imageRepository
                .findByProductIdOrderBySortOrderAsc(productId);
    }

    // ══════════════════════════════════════════════════════════════
    // ATTRIBUTE OPERATIONS
    // ══════════════════════════════════════════════════════════════

    // ── ADD attribute (spec) ──────────────────────────────────────
    @Transactional
    public ProductAttribute addAttribute(Long productId,
                                         Long userId,
                                         String key,
                                         String value) {

        Product product = getProductById(productId);

        if (!product.getVendor().getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized");
        }

        ProductAttribute attr = ProductAttribute.builder()
                .product(product)
                .attrKey(key)
                .attrValue(value)
                .displayOrder(0)
                .build();

        return attributeRepository.save(attr);
    }

    // ── GET attributes for product ────────────────────────────────
    public List<ProductAttribute> getAttributes(Long productId) {
        return attributeRepository
                .findByProductIdOrderByDisplayOrderAsc(productId);
    }

    // ── GET vendor's own products ─────────────────────────────────
    public List<Product> getVendorProducts(Long userId) {
        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));
        return productRepository
                .findByVendorIdAndIsActiveTrue(vendor.getId());
    }

    // ── Admin: feature/unfeature a product ───────────────────────
    @Transactional
    public Product toggleFeatured(Long productId) {
        Product product = getProductById(productId);
        product.setIsFeatured(!product.getIsFeatured());
        return productRepository.save(product);
    }

    // ── Admin: activate/deactivate a product ─────────────────────
    @Transactional
    public Product toggleActive(Long productId) {
        Product product = getProductById(productId);
        product.setIsActive(!product.getIsActive());
        return productRepository.save(product);
    }

    // ── LOW STOCK alert — for vendor dashboard ────────────────────
    public List<ProductVariant> getLowStockVariants() {
        return variantRepository
                .findByStockQtyLessThanAndIsActiveTrue(10);
    }
}