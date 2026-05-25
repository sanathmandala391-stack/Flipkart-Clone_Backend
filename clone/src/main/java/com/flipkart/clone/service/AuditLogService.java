package com.flipkart.clone.service;

import com.flipkart.clone.entity.AuditLog;
import com.flipkart.clone.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // ── GET all audit logs paginated ──────────────────────────────
    public Page<AuditLog> getAllLogs(int page, int size) {
        return auditLogRepository
                .findAllByOrderByCreatedAtDesc(
                        PageRequest.of(page, size));
    }

    // ── GET logs by admin ─────────────────────────────────────────
    public Page<AuditLog> getLogsByAdmin(Long adminId,
                                         int page, int size) {
        return auditLogRepository
                .findByActorUserIdOrderByCreatedAtDesc(
                        adminId, PageRequest.of(page, size));
    }
}