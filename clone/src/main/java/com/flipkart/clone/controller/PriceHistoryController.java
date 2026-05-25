package com.flipkart.clone.controller;

import com.flipkart.clone.entity.PriceHistory;
import com.flipkart.clone.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceHistoryRepository priceHistoryRepository;

    // ── GET /api/price-history/variant/{variantId} ────────────────
    // Shows last 30 prices — powers price chart on product page
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<PriceHistory>> getHistory(
            @PathVariable Long variantId) {

        return ResponseEntity.ok(
                priceHistoryRepository
                        .findTop30ByProductVariantIdOrderByChangedAtDesc(
                                variantId));
    }
}