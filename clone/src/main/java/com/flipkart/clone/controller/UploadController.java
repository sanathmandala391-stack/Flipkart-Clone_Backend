//package com.flipkart.clone.controller;
//
//import com.flipkart.clone.service.CloudinaryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/upload")
//@RequiredArgsConstructor
//public class UploadController {
//
//    private final CloudinaryService cloudinaryService;
//
//    // ── POST /api/upload/product-image ────────────────────────────
//    // Vendor uploads product image → gets back URL
//    @PostMapping("/product-image")
//    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
//    public ResponseEntity<Map<String, String>> uploadProductImage(
//            @RequestParam("file") MultipartFile file) {
//        try {
//            String url = cloudinaryService.uploadImage(
//                    file, "products");
//            return ResponseEntity.ok(Map.of("url", url));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(
//                    Map.of("error", "Upload failed: " + e.getMessage()));
//        }
//    }
//
//    // ── POST /api/upload/profile-image ────────────────────────────
//    @PostMapping("/profile-image")
//    public ResponseEntity<Map<String, String>> uploadProfileImage(
//            @RequestParam("file") MultipartFile file) {
//        try {
//            String url = cloudinaryService.uploadImage(
//                    file, "profiles");
//            return ResponseEntity.ok(Map.of("url", url));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(
//                    Map.of("error", "Upload failed: " + e.getMessage()));
//        }
//    }
//}




package com.flipkart.clone.controller;

import com.flipkart.clone.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/product-image")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadProductImage(
            @RequestParam("file") MultipartFile file
    ) {

        try {

            String url = cloudinaryService.uploadImage(
                    file,
                    "products"
            );

            Map<String, String> response = new HashMap<>();
            response.put("url", url);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("file") MultipartFile file
    ) {

        try {

            String url = cloudinaryService.uploadImage(
                    file,
                    "profiles"
            );

            Map<String, String> response = new HashMap<>();
            response.put("url", url);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }
}