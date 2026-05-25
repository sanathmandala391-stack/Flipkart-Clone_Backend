//package com.flipkart.clone.entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.math.BigDecimal;
//
//@Entity
//@Table(name = "order_items")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class OrderItem {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @JsonIgnore
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "order_id", nullable = false)
//    private Order order;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_variant_id", nullable = false)
//    private ProductVariant productVariant;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "vendor_id", nullable = false)
//    private Vendor vendor;
//
//    @Column(nullable = false)
//    private Integer quantity;
//
//    // Snapshot price at time of order — product price may change later
//    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
//    private BigDecimal unitPrice;
//
//    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
//    private BigDecimal totalPrice;
//
//    @Enumerated(EnumType.STRING)
//    private ItemStatus status;
//
//    // Relationships
//    @OneToOne(mappedBy = "orderItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private ReturnRequest returnRequest;
//
//    public enum ItemStatus {
//        CONFIRMED, SHIPPED, DELIVERED, CANCELLED, RETURN_REQUESTED, RETURNED
//    }
//}
























package com.flipkart.clone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ FIXED: was @JsonIgnore — now exposes only id, createdAt
    //    so frontend can read item.order.id without circular refs
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({
            "orderItems", "user", "address",
            "coupon", "paymentDetails", "shipment",
            "hibernateLazyInitializer", "handler"
    })
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @OneToOne(mappedBy = "orderItem",
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private ReturnRequest returnRequest;

    public enum ItemStatus {
        CONFIRMED, SHIPPED, DELIVERED, CANCELLED,
        RETURN_REQUESTED, RETURNED
    }
}