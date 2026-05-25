package com.flipkart.clone.controller;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import com.flipkart.clone.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/vendor/bulk")
@PreAuthorize("hasRole('VENDOR')")
@RequiredArgsConstructor
public class BulkUploadController {

    private final ProductRepository      productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository     categoryRepository;
    private final VendorRepository       vendorRepository;
    private final SearchTagRepository    searchTagRepository;

    // ── POST /api/vendor/bulk/upload ─────────────────────────────
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> bulkUpload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails user) {

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".csv")) {
            throw new BadRequestException(
                    "Only CSV files are accepted");
        }

        Vendor vendor = vendorRepository
                .findByUserId(user.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vendor profile not found"));

        if (!vendor.getIsApproved()) {
            throw new BadRequestException(
                    "Your vendor account is not approved yet");
        }

        List<Map<String, String>> success = new ArrayList<>();
        List<Map<String, String>> errors  = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BadRequestException("CSV file is empty");
            }

            String[] headers = headerLine.split(",");
            String line;
            int rowNum = 1;

            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.trim().isEmpty()) continue;

                try {
                    // Parse CSV row
                    String[] values = parseCsvLine(line);
                    Map<String, String> row = new HashMap<>();
                    for (int i = 0;
                         i < headers.length && i < values.length;
                         i++) {
                        row.put(
                                headers[i].trim().toLowerCase(),
                                values[i].trim()
                        );
                    }

                    // Process the row
                    String productName = processRow(
                            row, vendor, rowNum);

                    Map<String, String> ok = new HashMap<>();
                    ok.put("row",     String.valueOf(rowNum));
                    ok.put("product", productName);
                    success.add(ok);

                } catch (Exception e) {
                    Map<String, String> err = new HashMap<>();
                    err.put("row",   String.valueOf(rowNum));
                    err.put("error", e.getMessage());
                    errors.add(err);
                }
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException(
                    "Failed to parse CSV: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRows",    success.size() + errors.size());
        result.put("successCount", success.size());
        result.put("errorCount",   errors.size());
        result.put("success",      success);
        result.put("errors",       errors);
        result.put("message",
                success.size() + " products created successfully. " +
                        errors.size() + " rows had errors.");

        return ResponseEntity.ok(result);
    }

    // ── GET /api/vendor/bulk/template ────────────────────────────
    @GetMapping("/template")
    public ResponseEntity<String> getTemplate() {
        String csv =
                "product_name,brand,category_name,description," +
                        "sku,price,mrp,stock_qty,size,color,color_hex," +
                        "image_url,tags\n" +
                        "iPhone 15 Pro Max,Apple,Electronics," +
                        "Latest iPhone with A17 Pro chip," +
                        "IPH15PM-BLK-256,134900,139900,50," +
                        "256GB,Black Titanium,#1C1C1E," +
                        "https://example.com/image.jpg," +
                        "iphone;apple;smartphone\n" +
                        "Samsung Galaxy S24 Ultra,Samsung,Electronics," +
                        "200MP camera flagship," +
                        "S24U-BLK-256,124999,134999,30," +
                        "256GB,Titanium Black,#2D2D2D," +
                        "https://example.com/image2.jpg," +
                        "samsung;android;flagship";

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition",
                        "attachment; filename=flipkart_product_template.csv")
                .body(csv);
    }

    // ── Process one CSV row ───────────────────────────────────────
    private String processRow(Map<String, String> row,
                              Vendor vendor,
                              int rowNum) {

        String productName = row.get("product_name");
        if (productName == null || productName.isEmpty()) {
            throw new RuntimeException("product_name is required");
        }

        String brand = row.get("brand");
        if (brand == null || brand.isEmpty()) {
            throw new RuntimeException("brand is required");
        }

        String priceStr = row.get("price");
        String mrpStr   = row.get("mrp");
        String stockStr = row.get("stock_qty");

        if (priceStr == null || priceStr.isEmpty()) {
            throw new RuntimeException("price is required");
        }
        if (mrpStr == null || mrpStr.isEmpty()) {
            throw new RuntimeException("mrp is required");
        }
        if (stockStr == null || stockStr.isEmpty()) {
            throw new RuntimeException("stock_qty is required");
        }

        BigDecimal price;
        BigDecimal mrp;
        Integer stockQty;

        try {
            price    = new BigDecimal(priceStr);
            mrp      = new BigDecimal(mrpStr);
            stockQty = Integer.parseInt(stockStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "price, mrp, stock_qty must be numbers");
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("price must be > 0");
        }
        if (price.compareTo(mrp) > 0) {
            throw new RuntimeException(
                    "price cannot be greater than mrp");
        }

        // Find or use default category
        String categoryName = row.getOrDefault(
                "category_name", "Electronics");
        Category category = categoryRepository
                .findAll()
                .stream()
                .filter(c -> c.getName()
                        .equalsIgnoreCase(categoryName))
                .findFirst()
                .orElse(categoryRepository.findAll()
                        .stream().findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No categories found. Create one first.")));

        // Check if product already exists (same vendor + name)
        Optional<Product> existing = productRepository
                .findByVendorIdAndIsActiveTrue(vendor.getId())
                .stream()
                .filter(p -> p.getName()
                        .equalsIgnoreCase(productName))
                .findFirst();

        Product product;
        if (existing.isPresent()) {
            product = existing.get();
        } else {
            // Create new product
            product = Product.builder()
                    .vendor(vendor)
                    .category(category)
                    .name(productName)
                    .brand(brand)
                    .description(row.getOrDefault(
                            "description", ""))
                    .isActive(true)
                    .isFeatured(false)
                    .avgRating(0.0)
                    .totalReviews(0)
                    .build();
            product = productRepository.save(product);

            // Add image if provided
            String imageUrl = row.get("image_url");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .url(imageUrl)
                        .isPrimary(true)
                        .sortOrder(0)
                        .build();
                imageRepository.save(image);
            }

            // Add search tags
            String tagsStr = row.getOrDefault("tags", "");
            if (!tagsStr.isEmpty()) {
                String[] tags = tagsStr.split(";");
                for (String tag : tags) {
                    if (!tag.trim().isEmpty()) {
                        SearchTag searchTag = SearchTag.builder()
                                .product(product)
                                .tag(tag.trim().toLowerCase())
                                .build();
                        searchTagRepository.save(searchTag);
                    }
                }
            }
        }

        // Add variant
        String sku = row.getOrDefault("sku",
                product.getId() + "-" + rowNum);

        // Check if SKU already exists
        if (variantRepository.findBySku(sku).isPresent()) {
            sku = sku + "-" + rowNum;
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(sku)
                .price(price)
                .mrp(mrp)
                .stockQty(stockQty)
                .size(row.getOrDefault("size", ""))
                .color(row.getOrDefault("color", ""))
                .colorHex(row.getOrDefault("color_hex", ""))
                .isActive(true)
                .build();

        variantRepository.save(variant);
        return productName;
    }

    // ── Parse CSV line (handles quoted commas) ────────────────────
    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }
}