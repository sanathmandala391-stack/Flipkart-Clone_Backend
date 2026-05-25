package com.flipkart.clone.controller;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── GET /api/user/profile ─────────────────────────────────────
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                userService.getProfile(user.getUserId()));
    }

    // ── PUT /api/user/profile ─────────────────────────────────────
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                userService.updateProfile(user.getUserId(), body));
    }

    // ── PUT /api/user/change-password ─────────────────────────────
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        userService.changePassword(
                user.getUserId(),
                body.get("oldPassword"),
                body.get("newPassword"));

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully"));
    }

    // ── GET /api/user/addresses ───────────────────────────────────
    @GetMapping("/addresses")
    public ResponseEntity<List<Address>> getAddresses(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                userService.getAddresses(user.getUserId()));
    }

    // ── POST /api/user/addresses ──────────────────────────────────
    @PostMapping("/addresses")
    public ResponseEntity<Address> addAddress(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                userService.addAddress(user.getUserId(), body));
    }

    // ── PUT /api/user/addresses/{id} ──────────────────────────────
    @PutMapping("/addresses/{id}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                userService.updateAddress(id, user.getUserId(), body));
    }

    // ── DELETE /api/user/addresses/{id} ───────────────────────────
    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Map<String, String>> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        userService.deleteAddress(id, user.getUserId());
        return ResponseEntity.ok(
                Map.of("message", "Address deleted"));
    }

    // ── GET /api/user/notifications ───────────────────────────────
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                userService.getNotifications(user.getUserId()));
    }

    // ── GET /api/user/notifications/unread-count ──────────────────
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(Map.of("count",
                userService.getUnreadCount(user.getUserId())));
    }

    // ── PUT /api/user/notifications/read-all ──────────────────────
    @PutMapping("/notifications/read-all")
    public ResponseEntity<Map<String, String>> markAllRead(
            @AuthenticationPrincipal CustomUserDetails user) {

        userService.markAllRead(user.getUserId());
        return ResponseEntity.ok(
                Map.of("message", "All notifications marked as read"));
    }

    // ── PUT /api/user/notifications/{id}/read ─────────────────────
    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Map<String, String>> markOneRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        userService.markOneRead(id, user.getUserId());
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    // ── POST /api/user/recently-viewed/{productId} ────────────────
    @PostMapping("/recently-viewed/{productId}")
    public ResponseEntity<Map<String, String>> trackView(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails user) {

        userService.trackView(user.getUserId(), productId);
        return ResponseEntity.ok(Map.of("message", "Tracked"));
    }

    // ── GET /api/user/recently-viewed ────────────────────────────
    @GetMapping("/recently-viewed")
    public ResponseEntity<List<RecentlyViewed>> getRecentlyViewed(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                userService.getRecentlyViewed(user.getUserId()));
    }
}