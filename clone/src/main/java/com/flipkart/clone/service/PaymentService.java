package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Value("${app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.razorpay.key-secret}")
    private String razorpayKeySecret;

    // ── STEP 1: Create Razorpay order ────────────────────────────
    @Transactional
    public Map<String, Object> createRazorpayOrder(Long orderId,
                                                   Long userId)
            throws RazorpayException {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized");
        }

        // Amount in paise (multiply by 100)
        int amountInPaise = order.getFinalAmount()
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        // Create Razorpay order via SDK
        RazorpayClient client = new RazorpayClient(
                razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount",   amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt",  "order_" + orderId);

        com.razorpay.Order razorpayOrder =
                client.orders.create(orderRequest);

        String razorpayOrderId = razorpayOrder.get("id");

        // Save Payment entity with INITIATED status
        Payment payment = Payment.builder()
                .order(order)
                .razorpayOrderId(razorpayOrderId)
                .amount(order.getFinalAmount())
                .status(Payment.PaymentStatus.INITIATED)
                .build();
        paymentRepository.save(payment);

        // Return to frontend — needed to open Razorpay widget
        Map<String, Object> response = new HashMap<>();
        response.put("razorpayOrderId", razorpayOrderId);
        response.put("amount",          amountInPaise);
        response.put("currency",        "INR");
        response.put("keyId",           razorpayKeyId);
        response.put("orderId",         orderId);
        return response;
    }

    // ── STEP 2: Verify payment (called after Razorpay widget) ────
    @Transactional
    public Map<String, Object> verifyPayment(String razorpayOrderId,
                                             String razorpayPaymentId,
                                             String razorpaySignature)
            throws Exception {

        // 1. Verify HMAC SHA256 signature
        String payload = razorpayOrderId + "|" + razorpayPaymentId;

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                razorpayKeySecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        mac.init(keySpec);

        byte[] hash = mac.doFinal(
                payload.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexHash = new StringBuilder();
        for (byte b : hash) {
            hexHash.append(String.format("%02x", b));
        }

        // 2. Compare generated signature with Razorpay's signature
        if (!hexHash.toString().equals(razorpaySignature)) {
            throw new BadRequestException(
                    "Payment verification failed — invalid signature");
        }

        // 3. Update payment record
        Payment payment = paymentRepository
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found"));

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 4. Update order status
        Order order = payment.getOrder();
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setPaymentStatus(Order.PaymentStatus.SUCCESS);
        orderRepository.save(order);

        // 5. Notify user
        Notification notification = Notification.builder()
                .user(order.getUser())
                .type(Notification.NotifType.PAYMENT)
                .title("Payment Successful!")
                .message("₹" + payment.getAmount()
                        + " paid for order #" + order.getId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        // 6. Send confirmation email
        emailService.sendOrderConfirmation(
                order.getUser().getEmail(),
                order.getId().toString(),
                payment.getAmount().toString()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message",   "Payment verified successfully");
        response.put("orderId",   order.getId());
        response.put("paymentId", razorpayPaymentId);
        response.put("status",    "SUCCESS");
        return response;
    }

    // ── GET payment by order ID ───────────────────────────────────
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found"));
    }
}