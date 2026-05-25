package com.flipkart.clone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    @JsonIgnore
    private User actorUser;      // which admin did the action

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(name = "entity_type")
    private String entityType;   // "Vendor", "User", "Product"

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_value", columnDefinition = "JSON")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "JSON")
    private String newValue;

    @Column(name = "ip_address")
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum AuditAction {
        BAN_USER, UNBAN_USER, APPROVE_VENDOR, REJECT_VENDOR,
        SUSPEND_VENDOR, EDIT_PRODUCT, DELETE_PRODUCT,
        CREATE_COUPON, FEATURE_PRODUCT
    }
}