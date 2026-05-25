package com.flipkart.clone.controller;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ── GET /api/admin/users ──────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    // ── PUT /api/admin/users/{id}/toggle-ban ──────────────────────
    @PutMapping("/users/{id}/toggle-ban")
    public ResponseEntity<User> toggleBan(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails admin) {
        return ResponseEntity.ok(
                adminService.toggleUserBan(id, admin.getUserId()));
    }

    // ── GET /api/admin/vendors/pending ────────────────────────────
    @GetMapping("/vendors/pending")
    public ResponseEntity<List<Vendor>> getPendingVendors() {
        return ResponseEntity.ok(adminService.getPendingVendors());
    }

    // ── GET /api/admin/vendors ────────────────────────────────────
    @GetMapping("/vendors")
    public ResponseEntity<List<Vendor>> getAllVendors() {
        return ResponseEntity.ok(adminService.getAllVendors());
    }

    // ── PUT /api/admin/vendors/{id}/approve ───────────────────────
    @PutMapping("/vendors/{id}/approve")
    public ResponseEntity<Vendor> approveVendor(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails admin) {
        return ResponseEntity.ok(
                adminService.approveVendor(id, admin.getUserId()));
    }

    // ── PUT /api/admin/vendors/{id}/reject ────────────────────────
    @PutMapping("/vendors/{id}/reject")
    public ResponseEntity<Vendor> rejectVendor(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails admin) {
        return ResponseEntity.ok(
                adminService.rejectVendor(id, admin.getUserId()));
    }

    // ── GET /api/admin/coupons ────────────────────────────────────
    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getCoupons() {
        return ResponseEntity.ok(adminService.getAllCoupons());
    }

    // ── POST /api/admin/coupons ───────────────────────────────────
    @PostMapping("/coupons")
    public ResponseEntity<Coupon> createCoupon(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(adminService.createCoupon(body));
    }

    // ── PUT /api/admin/coupons/{id}/toggle ───────────────────────
    @PutMapping("/coupons/{id}/toggle")
    public ResponseEntity<Coupon> toggleCoupon(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleCoupon(id));
    }

    // ── GET /api/admin/banners ────────────────────────────────────
    @GetMapping("/banners")
    public ResponseEntity<List<Banner>> getBanners() {
        return ResponseEntity.ok(adminService.getAllBanners());
    }

    // ── POST /api/admin/banners ───────────────────────────────────
    @PostMapping("/banners")
    public ResponseEntity<Banner> createBanner(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(adminService.createBanner(body));
    }

    // ── DELETE /api/admin/banners/{id} ────────────────────────────
    @DeleteMapping("/banners/{id}")
    public ResponseEntity<Map<String, String>> deleteBanner(
            @PathVariable Long id) {
        adminService.deleteBanner(id);
        return ResponseEntity.ok(Map.of("message", "Banner deleted"));
    }

    // ── GET /api/admin/analytics ──────────────────────────────────
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        return ResponseEntity.ok(
                adminService.getPlatformAnalytics());
    }
}