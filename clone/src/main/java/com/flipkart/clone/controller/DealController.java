package com.flipkart.clone.controller;

import com.flipkart.clone.entity.Deal;
import com.flipkart.clone.service.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    // PUBLIC
    @GetMapping
    public ResponseEntity<List<Deal>> getActiveDeals() {
        return ResponseEntity.ok(
                dealService.getActiveDeals());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Deal>> getByType(
            @PathVariable String type) {
        return ResponseEntity.ok(
                dealService.getDealsByType(type));
    }

    // ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Deal> create(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(
                dealService.createDeal(body));
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Deal> toggle(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                dealService.toggleDeal(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id) {
        dealService.deleteDeal(id);
        return ResponseEntity.ok(
                Map.of("message", "Deal deleted"));
    }
}