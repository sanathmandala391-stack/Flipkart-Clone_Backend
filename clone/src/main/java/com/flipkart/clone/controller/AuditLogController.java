package com.flipkart.clone.controller;

import com.flipkart.clone.entity.AuditLog;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    // ── GET /api/admin/audit-logs ─────────────────────────────────
    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                auditLogService.getAllLogs(page, size));
    }

    // ── GET /api/admin/audit-logs/my ─────────────────────────────
    @GetMapping("/my")
    public ResponseEntity<Page<AuditLog>> getMyLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails admin) {

        return ResponseEntity.ok(
                auditLogService.getLogsByAdmin(
                        admin.getUserId(), page, size));
    }
}