// ReviewVoteRepository.java
package com.flipkart.clone.repository;

import com.flipkart.clone.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
    Boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
    Long countByReviewIdAndVoteType(Long reviewId, ReviewVote.VoteType voteType);
}