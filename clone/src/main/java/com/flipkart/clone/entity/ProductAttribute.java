package com.flipkart.clone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_attributes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(name = "attr_key", nullable = false)
    private String attrKey;          // e.g. "RAM"

    @Column(name = "attr_value", nullable = false)
    private String attrValue;        // e.g. "8 GB"

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}