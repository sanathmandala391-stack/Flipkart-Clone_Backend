package com.flipkart.clone.controller;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    // ── POST /api/vendor/register ─────────────────────────────────
    // Any logged-in user can apply to be a vendor
    @PostMapping("/register")
    public ResponseEntity<Vendor> register(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                vendorService.registerVendor(user.getUserId(), body));
    }

    // ── GET /api/vendor/profile ───────────────────────────────────
    @GetMapping("/profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Vendor> getProfile(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                vendorService.getVendorProfile(user.getUserId()));
    }

    // ── PUT /api/vendor/profile ───────────────────────────────────
    @PutMapping("/profile")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Vendor> updateProfile(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                vendorService.updateProfile(user.getUserId(), body));
    }

    // ── GET /api/vendor/orders ────────────────────────────────────
    @GetMapping("/orders")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<OrderItem>> getOrders(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                vendorService.getOrders(user.getUserId()));
    }

    // ── POST /api/vendor/orders/{orderId}/ship ────────────────────
    @PostMapping("/orders/{orderId}/ship")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Shipment> shipOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                vendorService.shipOrder(
                        orderId, user.getUserId(), body));
    }

    // ── GET /api/vendor/analytics ─────────────────────────────────
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                vendorService.getAnalytics(user.getUserId()));
    }

    // ── GET /api/vendor/payouts ───────────────────────────────────
    @GetMapping("/payouts")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<VendorPayout>> getPayouts(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                vendorService.getPayouts(user.getUserId()));
    }

    // ── GET /api/vendor/returns ───────────────────────────────────
    @GetMapping("/returns")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<ReturnRequest>> getReturns(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                vendorService.getReturnRequests(user.getUserId()));
    }
}