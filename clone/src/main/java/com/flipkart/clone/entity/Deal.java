////package com.flipkart.clone.entity;
////
////import jakarta.persistence.*;
////import lombok.*;
////import java.math.BigDecimal;
////import java.time.LocalDateTime;
////
////@Entity
////@Table(name = "deals")
////@Data @NoArgsConstructor @AllArgsConstructor @Builder
////public class Deal {
////
////    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long id;
////
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "product_variant_id", nullable = false)
////    private ProductVariant productVariant;
////
////    @Column(name = "deal_price", nullable = false, precision = 10, scale = 2)
////    private BigDecimal dealPrice;
////
////    @Enumerated(EnumType.STRING)
////    @Column(name = "deal_type")
////    private DealType dealType;
////
////    @Column(name = "max_qty_per_user")
////    private Integer maxQtyPerUser;
////
////    @Column(name = "total_qty")
////    private Integer totalQty;
////
////    @Column(name = "sold_qty")
////    private Integer soldQty = 0;
////
////    @Column(name = "is_active")
////    private Boolean isActive = true;
////
////    @Column(name = "start_at")
////    private LocalDateTime startAt;
////
////    @Column(name = "end_at")
////    private LocalDateTime endAt;
////
////    public enum DealType { FLASH, DEAL_OF_THE_DAY, WEEKLY }
////}
//
//
//
//
//package com.flipkart.clone.entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.*;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "deals")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Deal {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_variant_id", nullable = false)
//    @JsonIgnore
//    private ProductVariant productVariant;
//
//    @Column(name = "deal_price", nullable = false, precision = 10, scale = 2)
//    private BigDecimal dealPrice;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "deal_type")
//    private DealType dealType;
//
//    @Column(name = "max_qty_per_user")
//    private Integer maxQtyPerUser;
//
//    @Column(name = "total_qty")
//    private Integer totalQty;
//
//    @Column(name = "sold_qty")
//    private Integer soldQty = 0;
//
//    @Column(name = "is_active")
//    private Boolean isActive = true;
//
//    @Column(name = "start_at")
//    private LocalDateTime startAt;
//
//    @Column(name = "end_at")
//    private LocalDateTime endAt;
//
//    public enum DealType {
//        FLASH,
//        DEAL_OF_THE_DAY,
//        WEEKLY
//    }
//}











































package com.flipkart.clone.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ FIXED: removed @JsonIgnore, added EAGER fetch
    //    so productVariant + its product are serialized in response
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_variant_id", nullable = false)
    @JsonIgnoreProperties({
            "hibernateLazyInitializer", "handler",
            "reviews", "orderItems", "cartItems",
            "wishlistItems", "dealItems"
    })
    private ProductVariant productVariant;

    @Column(name = "deal_price", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal dealPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "deal_type")
    private DealType dealType;

    @Column(name = "max_qty_per_user")
    private Integer maxQtyPerUser;

    @Column(name = "total_qty")
    private Integer totalQty;

    @Column(name = "sold_qty")
    private Integer soldQty = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    public enum DealType {
        FLASH,
        DEAL_OF_THE_DAY,
        WEEKLY
    }
}