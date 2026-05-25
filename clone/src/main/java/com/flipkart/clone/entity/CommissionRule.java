package com.flipkart.clone.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "commission_rules")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CommissionRule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "commission_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPct;    // e.g. 8.50 means 8.5%

    @Column(name = "fixed_fee", precision = 6, scale = 2)
    private BigDecimal fixedFee = BigDecimal.ZERO;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "is_active")
    private Boolean isActive = true;
}