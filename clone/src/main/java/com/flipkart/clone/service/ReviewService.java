package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    // ── GET reviews for a product ─────────────────────────────────
    public Page<Review> getProductReviews(Long productId,
                                          int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return reviewRepository
                .findByProductIdOrderByCreatedAtDesc(
                        productId, pageable);
    }

    // ── GET review summary (avg + count breakdown) ────────────────
    public Map<String, Object> getReviewSummary(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Map<String, Object> summary = new HashMap<>();
        summary.put("avgRating",    product.getAvgRating());
        summary.put("totalReviews", product.getTotalReviews());
        return summary;
    }

    // ── WRITE a review ────────────────────────────────────────────
    @Transactional
    public Review writeReview(Long userId, Long productId,
                              Map<String, Object> body) {

        // One review per product per user
        if (reviewRepository
                .existsByProductIdAndUserId(productId, userId)) {
            throw new BadRequestException(
                    "You have already reviewed this product");
        }

        Integer rating = Integer.valueOf(body.get("rating").toString());
        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Check if verified purchase
        Boolean isVerified = orderItemRepository
                .hasUserPurchasedProduct(userId, productId);

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(rating)
                .title(body.getOrDefault("title", "").toString())
                .body(body.getOrDefault("body", "").toString())
                .imagesJson(body.containsKey("imagesJson")
                        ? body.get("imagesJson").toString() : null)
                .isVerifiedPurchase(isVerified)
                .build();

        Review saved = reviewRepository.save(review);

        // Recalculate product avg rating
        updateProductRating(product);

        return saved;
    }

    // ── DELETE review ─────────────────────────────────────────────
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new BadRequestException(
                    "You can only delete your own review");
        }

        Product product = review.getProduct();
        reviewRepository.delete(review);
        updateProductRating(product);
    }

    // ── VOTE on review (helpful / not helpful) ────────────────────
    @Transactional
    public Map<String, Object> voteReview(Long reviewId,
                                          Long userId,
                                          String voteType) {

        if (reviewVoteRepository
                .existsByReviewIdAndUserId(reviewId, userId)) {
            throw new BadRequestException(
                    "You have already voted on this review");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Review not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        ReviewVote vote = ReviewVote.builder()
                .review(review)
                .user(user)
                .voteType(ReviewVote.VoteType.valueOf(
                        voteType.toUpperCase()))
                .build();

        reviewVoteRepository.save(vote);

        Long helpfulCount = reviewVoteRepository
                .countByReviewIdAndVoteType(
                        reviewId, ReviewVote.VoteType.HELPFUL);

        Map<String, Object> response = new HashMap<>();
        response.put("message",      "Vote recorded");
        response.put("helpfulCount", helpfulCount);
        return response;
    }

    // ── Helper: update product avg rating ─────────────────────────
    private void updateProductRating(Product product) {
        Double avg = reviewRepository
                .getAverageRatingByProductId(product.getId());
        Long count = reviewRepository
                .countByProductId(product.getId());

        product.setAvgRating(avg == null ? 0.0 : avg);
        product.setTotalReviews(count == null ? 0 : count.intValue());
        productRepository.save(product);
    }
}