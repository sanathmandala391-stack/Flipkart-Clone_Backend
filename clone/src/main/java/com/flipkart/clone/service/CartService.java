package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    // ── GET cart with summary ─────────────────────────────────────
    public Map<String, Object> getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        BigDecimal subtotal = items.stream()
                .map(item -> item.getProductVariant()
                        .getPrice()
                        .multiply(BigDecimal.valueOf(
                                item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal mrpTotal = items.stream()
                .map(item -> item.getProductVariant()
                        .getMrp()
                        .multiply(BigDecimal.valueOf(
                                item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal savings      = mrpTotal.subtract(subtotal);
        BigDecimal deliveryFee  = subtotal.compareTo(
                BigDecimal.valueOf(500)) >= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(40);

        Map<String, Object> response = new HashMap<>();
        response.put("items",       items);
        response.put("subtotal",    subtotal);
        response.put("mrpTotal",    mrpTotal);
        response.put("savings",     savings);
        response.put("deliveryFee", deliveryFee);
        response.put("total",       subtotal.add(deliveryFee));
        response.put("itemCount",   items.size());
        return response;
    }

    // ── ADD item to cart ──────────────────────────────────────────
    @Transactional
    public CartItem addToCart(Long userId, Long variantId, Integer qty) {

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product variant not found"));

        if (!variant.getIsActive()) {
            throw new BadRequestException("This product is not available");
        }
        if (variant.getStockQty() < qty) {
            throw new BadRequestException(
                    "Only " + variant.getStockQty() + " items left in stock");
        }

        // If already in cart — just update quantity
        Optional<CartItem> existing =
                cartItemRepository.findByUserIdAndProductVariantId(
                        userId, variantId);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + qty;

            if (variant.getStockQty() < newQty) {
                throw new BadRequestException(
                        "Cannot add more. Only "
                                + variant.getStockQty() + " in stock");
            }
            item.setQuantity(newQty);
            return cartItemRepository.save(item);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        CartItem item = CartItem.builder()
                .user(user)
                .productVariant(variant)
                .quantity(qty)
                .build();

        return cartItemRepository.save(item);
    }

    // ── UPDATE quantity ───────────────────────────────────────────
    @Transactional
    public CartItem updateQuantity(Long cartItemId,
                                   Long userId,
                                   Integer qty) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart item not found"));

        // Make sure this cart item belongs to this user
        if (!item.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your cart item");
        }

        if (qty <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        if (item.getProductVariant().getStockQty() < qty) {
            throw new BadRequestException(
                    "Only " + item.getProductVariant().getStockQty()
                            + " items in stock");
        }

        item.setQuantity(qty);
        return cartItemRepository.save(item);
    }

    // ── REMOVE item from cart ─────────────────────────────────────
    @Transactional
    public void removeFromCart(Long cartItemId, Long userId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your cart item");
        }

        cartItemRepository.delete(item);
    }

    // ── CLEAR entire cart ─────────────────────────────────────────
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    // ── APPLY coupon ──────────────────────────────────────────────
    public Map<String, Object> applyCoupon(Long userId,
                                           String code,
                                           BigDecimal orderAmount) {

        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() ->
                        new BadRequestException("Invalid or expired coupon"));

        // Check min order amount
        if (coupon.getMinOrderAmount() != null &&
                orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException(
                    "Minimum order amount of ₹"
                            + coupon.getMinOrderAmount()
                            + " required for this coupon");
        }

        // Check if user already used this coupon
        if (couponUsageRepository
                .existsByCouponIdAndUserId(coupon.getId(), userId)) {
            throw new BadRequestException(
                    "You have already used this coupon");
        }

        // Check max uses
        if (coupon.getMaxUses() != null &&
                coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new BadRequestException("Coupon usage limit reached");
        }

        // Calculate discount
        BigDecimal discount;
        if (coupon.getDiscountType() == Coupon.DiscountType.FLAT) {
            discount = coupon.getDiscountValue();
        } else {
            // PERCENTAGE
            discount = orderAmount.multiply(
                            coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));

            // Apply max discount cap
            if (coupon.getMaxDiscountAmount() != null &&
                    discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        }

        BigDecimal finalAmount = orderAmount.subtract(discount);

        Map<String, Object> response = new HashMap<>();
        response.put("couponCode",   code);
        response.put("discount",     discount);
        response.put("finalAmount",  finalAmount);
        response.put("message",      "Coupon applied successfully!");
        return response;
    }
}