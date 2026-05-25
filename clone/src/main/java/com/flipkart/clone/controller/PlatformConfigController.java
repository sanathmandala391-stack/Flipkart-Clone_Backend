package com.flipkart.clone.controller;

import com.flipkart.clone.entity.PlatformConfig;
import com.flipkart.clone.security.CustomUserDetails;
import com.flipkart.clone.service.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PlatformConfigController {

    private final PlatformConfigService configService;

    // ── GET /api/admin/config ─────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<PlatformConfig>> getAll() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    // ── GET /api/admin/config/{key} ───────────────────────────────
    @GetMapping("/{key}")
    public ResponseEntity<PlatformConfig> getByKey(
            @PathVariable String key) {
        return ResponseEntity.ok(configService.getConfig(key));
    }

    // ── POST /api/admin/config ────────────────────────────────────
    @PostMapping
    public ResponseEntity<PlatformConfig> set(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails admin) {

        return ResponseEntity.ok(
                configService.setConfig(admin.getUserId(), body));
    }

    // ── DELETE /api/admin/config/{key} ────────────────────────────
    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable String key) {
        configService.deleteConfig(key);
        return ResponseEntity.ok(
                Map.of("message", "Config deleted: " + key));
    }
}