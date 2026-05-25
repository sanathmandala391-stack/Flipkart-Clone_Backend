package com.flipkart.clone.controller;

import com.flipkart.clone.entity.CartItem;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ── GET /api/cart ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(cartService.getCart(user.getUserId()));
    }

    // ── POST /api/cart/add ────────────────────────────────────────
    @PostMapping("/add")
    public ResponseEntity<CartItem> addToCart(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        Long    variantId = Long.valueOf(body.get("variantId").toString());
        Integer qty       = Integer.valueOf(
                body.getOrDefault("quantity", 1).toString());

        return ResponseEntity.ok(
                cartService.addToCart(user.getUserId(), variantId, qty));
    }

    // ── PUT /api/cart/{cartItemId} ────────────────────────────────
    @PutMapping("/{cartItemId}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        Integer qty = Integer.valueOf(body.get("quantity").toString());
        CartItem updated = cartService.updateQuantity(
                cartItemId, user.getUserId(), qty);

        if (updated == null) {
            return ResponseEntity.ok(
                    Map.of("message", "Item removed from cart"));
        }
        return ResponseEntity.ok(updated);
    }

    // ── DELETE /api/cart/{cartItemId} ─────────────────────────────
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Map<String, String>> removeItem(
            @PathVariable Long cartItemId,
            @AuthenticationPrincipal CustomUserDetails user) {

        cartService.removeFromCart(cartItemId, user.getUserId());
        return ResponseEntity.ok(
                Map.of("message", "Item removed from cart"));
    }

    // ── DELETE /api/cart/clear ────────────────────────────────────
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCart(
            @AuthenticationPrincipal CustomUserDetails user) {

        cartService.clearCart(user.getUserId());
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }

    // ── POST /api/cart/coupon ─────────────────────────────────────
    @PostMapping("/coupon")
    public ResponseEntity<Map<String, Object>> applyCoupon(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        String     code   = body.get("code").toString();
        BigDecimal amount = new BigDecimal(
                body.get("orderAmount").toString());

        return ResponseEntity.ok(
                cartService.applyCoupon(user.getUserId(), code, amount));
    }
}