package com.flipkart.clone.controller;

import com.flipkart.clone.entity.Wishlist;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // ── GET /api/wishlist ─────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Wishlist>> getWishlist(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                wishlistService.getWishlist(user.getUserId()));
    }

    // ── POST /api/wishlist/{productId} ────────────────────────────
    @PostMapping("/{productId}")
    public ResponseEntity<Wishlist> add(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                wishlistService.addToWishlist(
                        user.getUserId(), productId));
    }

    // ── DELETE /api/wishlist/{productId} ──────────────────────────
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, String>> remove(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails user) {

        wishlistService.removeFromWishlist(
                user.getUserId(), productId);
        return ResponseEntity.ok(
                Map.of("message", "Removed from wishlist"));
    }

    // ── GET /api/wishlist/{productId}/check ───────────────────────
    @GetMapping("/{productId}/check")
    public ResponseEntity<Map<String, Boolean>> check(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                wishlistService.isWishlisted(
                        user.getUserId(), productId));
    }
}