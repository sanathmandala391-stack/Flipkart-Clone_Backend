package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductQuestionService {

    private final ProductQuestionRepository questionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // PUBLIC
    public List<ProductQuestion> getApprovedQuestions(
            Long productId) {
        return questionRepository
                .findByProductIdAndIsApprovedTrue(productId);
    }

    // ADMIN
    public List<ProductQuestion> getPendingQuestions() {
        return questionRepository.findByIsApprovedFalse();
    }

    // USER — ask a question
    @Transactional
    public ProductQuestion askQuestion(Long userId,
                                       Long productId,
                                       String question) {

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        ProductQuestion pq = ProductQuestion.builder()
                .product(product)
                .askedBy(user)
                .question(question)
                .isApproved(false)
                .build();

        return questionRepository.save(pq);
    }

    // USER / VENDOR — answer a question
    @Transactional
    public ProductQuestion answerQuestion(Long questionId,
                                          Long userId,
                                          String answer) {

        ProductQuestion pq = questionRepository
                .findById(questionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Question not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        pq.setAnswer(answer);
        pq.setAnsweredBy(user);
        pq.setAnsweredAt(LocalDateTime.now());

        return questionRepository.save(pq);
    }

    // ADMIN — approve question
    @Transactional
    public ProductQuestion approveQuestion(Long questionId) {
        ProductQuestion pq = questionRepository
                .findById(questionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Question not found"));
        pq.setIsApproved(true);
        return questionRepository.save(pq);
    }

    // ADMIN — delete question
    @Transactional
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }
}