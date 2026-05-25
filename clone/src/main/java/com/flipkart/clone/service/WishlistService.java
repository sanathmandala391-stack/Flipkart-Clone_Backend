package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ── GET wishlist ──────────────────────────────────────────────
    public List<Wishlist> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    // ── ADD to wishlist ───────────────────────────────────────────
    @Transactional
    public Wishlist addToWishlist(Long userId, Long productId) {
        if (wishlistRepository
                .existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException(
                    "Product already in wishlist");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        return wishlistRepository.save(wishlist);
    }

    // ── REMOVE from wishlist ──────────────────────────────────────
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        if (!wishlistRepository
                .existsByUserIdAndProductId(userId, productId)) {
            throw new ResourceNotFoundException(
                    "Product not in wishlist");
        }
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }

    // ── CHECK if product is wishlisted ────────────────────────────
    public Map<String, Boolean> isWishlisted(Long userId,
                                             Long productId) {
        Boolean exists = wishlistRepository
                .existsByUserIdAndProductId(userId, productId);
        return Map.of("wishlisted", exists);
    }
}