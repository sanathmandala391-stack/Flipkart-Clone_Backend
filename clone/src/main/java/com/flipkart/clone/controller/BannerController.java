package com.flipkart.clone.controller;

import com.flipkart.clone.entity.Banner;
import com.flipkart.clone.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // PUBLIC
    @GetMapping
    public ResponseEntity<List<Banner>> getActive() {
        return ResponseEntity.ok(
                bannerService.getActiveBanners());
    }

    @GetMapping("/position/{position}")
    public ResponseEntity<List<Banner>> getByPosition(
            @PathVariable String position) {
        return ResponseEntity.ok(
                bannerService.getByPosition(position));
    }

    // ADMIN
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Banner>> getAll() {
        return ResponseEntity.ok(
                bannerService.getAllBanners());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Banner> create(
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(
                bannerService.createBanner(body));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Banner> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(
                bannerService.updateBanner(id, body));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(
                Map.of("message", "Banner deleted"));
    }
}