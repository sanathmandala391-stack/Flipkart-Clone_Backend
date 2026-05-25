package com.flipkart.clone.controller;

import com.flipkart.clone.entity.CommissionRule;
import com.flipkart.clone.service.CommissionRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/commission-rules")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CommissionRuleController {

    private final CommissionRuleService commissionRuleService;

    // ── GET /api/admin/commission-rules ───────────────────────────
    @GetMapping
    public ResponseEntity<List<CommissionRule>> getAll() {
        return ResponseEntity.ok(
                commissionRuleService.getAllRules());
    }

    // ── GET /api/admin/commission-rules/category/{categoryId} ─────
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<CommissionRule> getForCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(
                commissionRuleService.getRuleForCategory(categoryId));
    }

    // ── POST /api/admin/commission-rules ──────────────────────────
    @PostMapping
    public ResponseEntity<CommissionRule> create(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(
                commissionRuleService.createRule(body));
    }

    // ── DELETE /api/admin/commission-rules/{id} ───────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deactivate(
            @PathVariable Long id) {
        commissionRuleService.deactivateRule(id);
        return ResponseEntity.ok(
                Map.of("message", "Commission rule deactivated"));
    }
}