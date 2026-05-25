package com.flipkart.clone.controller;

import com.flipkart.clone.entity.Review;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ── GET /api/reviews/product/{productId} ──────────────────────
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<Review>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                reviewService.getProductReviews(productId, page, size));
    }

    // ── GET /api/reviews/product/{productId}/summary ──────────────
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @PathVariable Long productId) {
        return ResponseEntity.ok(
                reviewService.getReviewSummary(productId));
    }

    // ── POST /api/reviews/product/{productId} ─────────────────────
    @PostMapping("/product/{productId}")
    public ResponseEntity<Review> writeReview(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                reviewService.writeReview(
                        user.getUserId(), productId, body));
    }

    // ── DELETE /api/reviews/{reviewId} ────────────────────────────
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails user) {

        reviewService.deleteReview(reviewId, user.getUserId());
        return ResponseEntity.ok(Map.of("message", "Review deleted"));
    }

    // ── POST /api/reviews/{reviewId}/vote ─────────────────────────
    @PostMapping("/{reviewId}/vote")
    public ResponseEntity<Map<String, Object>> vote(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                reviewService.voteReview(
                        reviewId, user.getUserId(), body.get("voteType")));
    }
}