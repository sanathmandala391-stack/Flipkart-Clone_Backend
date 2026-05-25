package com.flipkart.clone.controller;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── POST /api/orders/checkout ─────────────────────────────────
//    @PostMapping("/checkout")
//    public ResponseEntity<Order> checkout(
//            @RequestBody Map<String, Object> body,
//            @AuthenticationPrincipal CustomUserDetails user) {
//
//        Long   addressId     = Long.valueOf(
//                body.get("addressId").toString());
//        String paymentMethod = body.getOrDefault(
//                "paymentMethod", "RAZORPAY").toString();
//        String couponCode    = body.containsKey("couponCode")
//                ? body.get("couponCode").toString() : null;
//
//        return ResponseEntity.ok(
//                orderService.placeOrder(
//                        user.getUserId(), addressId,
//                        paymentMethod, couponCode));
//    }


    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        System.out.println(body);

        Object addressObj = body.get("addressId");

        if (addressObj == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "addressId is required"));
        }

        Long addressId = Long.valueOf(addressObj.toString());

        String paymentMethod = body.getOrDefault(
                "paymentMethod",
                "RAZORPAY"
        ).toString();

        String couponCode = body.containsKey("couponCode")
                ? body.get("couponCode").toString()
                : null;

        return ResponseEntity.ok(
                orderService.placeOrder(
                        user.getUserId(),
                        addressId,
                        paymentMethod,
                        couponCode
                )
        );
    }

    // ── GET /api/orders ───────────────────────────────────────────
    @GetMapping
    public ResponseEntity<Page<Order>> getMyOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                orderService.getMyOrders(user.getUserId(), page, size));
    }

    // ── GET /api/orders/{id} ──────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                orderService.getOrderById(id, user.getUserId()));
    }

    // ── PUT /api/orders/{id}/cancel ───────────────────────────────
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                orderService.cancelOrder(id, user.getUserId()));
    }

    // ══════════════════════════════════════════════════════════════
    // VENDOR
    // ══════════════════════════════════════════════════════════════

    // ── GET /api/orders/vendor/items ──────────────────────────────
    @GetMapping("/vendor/items")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<OrderItem>> getVendorOrders(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                orderService.getVendorOrders(user.getUserId()));
    }

    // ── PUT /api/orders/vendor/items/{itemId}/status ──────────────
    @PutMapping("/vendor/items/{itemId}/status")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<OrderItem> updateItemStatus(
            @PathVariable Long itemId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                orderService.updateItemStatus(
                        itemId, user.getUserId(), body.get("status")));
    }

    // ══════════════════════════════════════════════════════════════
    // ADMIN
    // ══════════════════════════════════════════════════════════════

    // ── GET /api/orders/admin/all ─────────────────────────────────
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                orderService.getAllOrders(page, size));
    }

    // ── PUT /api/orders/admin/{id}/status ─────────────────────────
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(id, body.get("status")));
    }
}