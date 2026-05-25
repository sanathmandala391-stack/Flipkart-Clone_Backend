package com.flipkart.clone.controller;

import com.flipkart.clone.entity.ReturnRequest;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnRequestController {

    private final ReturnRequestService returnService;

    // USER — raise return
    @PostMapping("/order-item/{orderItemId}")
    public ResponseEntity<ReturnRequest> raise(
            @PathVariable Long orderItemId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                returnService.raiseReturn(
                        user.getUserId(), orderItemId, body));
    }

    // USER — my returns
    @GetMapping("/my")
    public ResponseEntity<List<ReturnRequest>> getMyReturns(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(
                returnService.getUserReturns(user.getUserId()));
    }

    // ADMIN — all returns
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReturnRequest>> getAll() {
        return ResponseEntity.ok(
                returnService.getAllReturns());
    }

    // ADMIN — pending
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReturnRequest>> getPending() {
        return ResponseEntity.ok(
                returnService.getPendingReturns());
    }

    // ADMIN — approve
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReturnRequest> approve(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                returnService.approveReturn(id));
    }

    // ADMIN — reject
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReturnRequest> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return ResponseEntity.ok(
                returnService.rejectReturn(
                        id,
                        body.getOrDefault(
                                "reason", "Policy violation")));
    }
}