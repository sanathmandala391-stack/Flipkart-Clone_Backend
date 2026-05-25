package com.flipkart.clone.controller;

import com.flipkart.clone.entity.Address;
import com.flipkart.clone.entity.User;
import com.flipkart.clone.repository.AddressRepository;
import com.flipkart.clone.repository.UserRepository;
import com.flipkart.clone.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // ── ADD ADDRESS ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Address> addAddress(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = Address.builder()
                .user(user)
                .name(body.get("name").toString())
                .phone(body.get("phone").toString())
                .line1(body.get("line1").toString())
                .line2(body.getOrDefault("line2", "").toString())
                .city(body.get("city").toString())
                .state(body.get("state").toString())
                .pincode(body.get("pincode").toString())
                .addressType(Address.AddressType.valueOf(
                        body.getOrDefault("addressType", "HOME").toString()
                ))
                .isDefault(Boolean.valueOf(
                        body.getOrDefault("isDefault", true).toString()
                ))
                .build();

        return ResponseEntity.ok(addressRepository.save(address));
    }

    // ── GET MY ADDRESSES ────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Address>> getMyAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                addressRepository.findByUserId(userDetails.getUserId())
        );
    }
}