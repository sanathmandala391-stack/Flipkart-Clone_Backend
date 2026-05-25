package com.flipkart.clone.controller;

import com.flipkart.clone.entity.VendorDocument;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.VendorDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor/documents")
@RequiredArgsConstructor
public class VendorDocumentController {

    private final VendorDocumentService documentService;

    // ── POST /api/vendor/documents — VENDOR ───────────────────────
    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<VendorDocument> upload(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                documentService.uploadDocument(
                        user.getUserId(), body));
    }

    // ── GET /api/vendor/documents/my — VENDOR ────────────────────
    @GetMapping("/my")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<VendorDocument>> getMyDocs(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                documentService.getMyDocuments(user.getUserId()));
    }

    // ── GET /api/vendor/documents/pending — ADMIN ─────────────────
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VendorDocument>> getPending() {
        return ResponseEntity.ok(
                documentService.getPendingDocuments());
    }

    // ── PUT /api/vendor/documents/{id}/verify — ADMIN ─────────────
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VendorDocument> verify(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails admin) {

        Boolean approve = (Boolean) body.get("approve");
        return ResponseEntity.ok(
                documentService.verifyDocument(
                        id, admin.getUserId(), approve));
    }
}