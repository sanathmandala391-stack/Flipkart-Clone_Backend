package com.flipkart.clone.controller;

import com.flipkart.clone.entity.Payment;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ── POST /api/payments/create-order ───────────────────────────
    // Called after placeOrder — creates Razorpay order
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user)
            throws Exception {

        Long orderId = Long.valueOf(body.get("orderId").toString());
        return ResponseEntity.ok(
                paymentService.createRazorpayOrder(
                        orderId, user.getUserId()));
    }

    // ── POST /api/payments/verify ─────────────────────────────────
    // Called from frontend after Razorpay widget success
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestBody Map<String, String> body)
            throws Exception {

        return ResponseEntity.ok(
                paymentService.verifyPayment(
                        body.get("razorpayOrderId"),
                        body.get("razorpayPaymentId"),
                        body.get("razorpaySignature")
                ));
    }

    // ── GET /api/payments/order/{orderId} ─────────────────────────
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getByOrder(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
                paymentService.getPaymentByOrderId(orderId));
    }
}