package com.flipkart.clone.controller;

import com.flipkart.clone.entity.ProductQuestion;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.ProductQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class ProductQuestionController {

    private final ProductQuestionService questionService;

    // PUBLIC
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductQuestion>> getApproved(
            @PathVariable Long productId) {
        return ResponseEntity.ok(
                questionService.getApprovedQuestions(productId));
    }

    // USER — ask
    @PostMapping("/product/{productId}")
    public ResponseEntity<ProductQuestion> ask(
            @PathVariable Long productId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                questionService.askQuestion(
                        user.getUserId(),
                        productId,
                        body.get("question")));
    }

    // USER / VENDOR — answer
    @PutMapping("/{id}/answer")
    public ResponseEntity<ProductQuestion> answer(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                questionService.answerQuestion(
                        id,
                        user.getUserId(),
                        body.get("answer")));
    }

    // ADMIN
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductQuestion>> getPending() {
        return ResponseEntity.ok(
                questionService.getPendingQuestions());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductQuestion> approve(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                questionService.approveQuestion(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(
                Map.of("message", "Question deleted"));
    }
}