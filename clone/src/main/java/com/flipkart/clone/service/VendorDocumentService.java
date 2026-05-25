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
public class VendorDocumentService {

    private final VendorDocumentRepository vendorDocumentRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    // ── UPLOAD document ───────────────────────────────────────────
    @Transactional
    public VendorDocument uploadDocument(Long userId,
                                         Map<String, Object> body) {

        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vendor profile not found"));

        VendorDocument doc = VendorDocument.builder()
                .vendor(vendor)
                .docType(VendorDocument.DocType.valueOf(
                        body.get("docType").toString()))
                .docUrl(body.get("docUrl").toString())
                .status(VendorDocument.DocStatus.PENDING)
                .build();

        return vendorDocumentRepository.save(doc);
    }

    // ── GET documents for a vendor ────────────────────────────────
    public List<VendorDocument> getMyDocuments(Long userId) {
        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vendor profile not found"));
        return vendorDocumentRepository
                .findByVendorId(vendor.getId());
    }

    // ── GET all pending documents (admin) ─────────────────────────
    public List<VendorDocument> getPendingDocuments() {
        return vendorDocumentRepository
                .findByStatus(VendorDocument.DocStatus.PENDING);
    }

    // ── VERIFY document (admin) ───────────────────────────────────
    @Transactional
    public VendorDocument verifyDocument(Long docId,
                                         Long adminId,
                                         Boolean approve) {

        VendorDocument doc = vendorDocumentRepository
                .findById(docId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document not found"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Admin not found"));

        doc.setStatus(approve
                ? VendorDocument.DocStatus.VERIFIED
                : VendorDocument.DocStatus.REJECTED);
        doc.setVerifiedBy(admin);
        doc.setVerifiedAt(java.time.LocalDateTime.now());

        return vendorDocumentRepository.save(doc);
    }
}