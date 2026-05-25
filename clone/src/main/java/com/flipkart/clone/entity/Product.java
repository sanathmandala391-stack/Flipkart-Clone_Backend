//package com.flipkart.clone.entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Table(name = "products")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Product {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "vendor_id", nullable = false)
//    private Vendor vendor;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "category_id", nullable = false)
//    private Category category;
//
//    @Column(nullable = false)
//    private String name;
//
//    @Column(columnDefinition = "TEXT")
//    private String description;
//
//    @Column(nullable = false)
//    private String brand;
//
//    @Column(name = "is_active")
//    private Boolean isActive = true;
//
//    @Column(name = "is_featured")
//    private Boolean isFeatured = false;
//
//    @Column(name = "avg_rating")
//    private Double avgRating = 0.0;
//
//    @Column(name = "total_reviews")
//    private Integer totalReviews = 0;
//
//    @CreationTimestamp
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    // Relationships
////    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
////    private List<ProductVariant> variants;
//
//    @OneToMany(
//            mappedBy = "product",
//            cascade = CascadeType.ALL,
//            fetch = FetchType.EAGER
//    )
//    private List<ProductVariant> variants;
//
////    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
////    private List<ProductImage> images;
//
//    @OneToMany(
//            mappedBy = "product",
//            cascade = CascadeType.ALL,
//            fetch = FetchType.EAGER
//    )
//    private List<ProductImage> images;
//
//
//
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<ProductAttribute> attributes;
//
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<Review> reviews;
//
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<SearchTag> searchTags;
//
////    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
////    private List<ProductImage> images;
//
//    // Add these two missing lists to Product.java
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<ProductQuestion> questions;
//
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<RecentlyViewed> recentlyViewed;
//}










































package com.flipkart.clone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "products"})
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "products"})
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String brand;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "avg_rating")
    private Double avgRating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ FIXED: @JsonIgnoreProperties breaks the
    //    Product → variants → Product circular reference
    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER
    )
    @JsonIgnoreProperties({"product", "cartItems",
            "orderItems", "priceHistories"})
    private List<ProductVariant> variants;

    // images are fine — no cycle here
    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER
    )
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProductAttribute> attributes;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SearchTag> searchTags;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProductQuestion> questions;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<RecentlyViewed> recentlyViewed;
}