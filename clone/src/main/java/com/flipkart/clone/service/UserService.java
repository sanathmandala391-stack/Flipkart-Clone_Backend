package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final NotificationRepository notificationRepository;
    private final RecentlyViewedRepository recentlyViewedRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    // ── GET profile ───────────────────────────────────────────────
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    // ── UPDATE profile ────────────────────────────────────────────
    @Transactional
    public User updateProfile(Long userId, Map<String, Object> body) {
        User user = getProfile(userId);

        if (body.containsKey("name"))
            user.setName(body.get("name").toString());
        if (body.containsKey("phone"))
            user.setPhone(body.get("phone").toString());
        if (body.containsKey("profileImageUrl"))
            user.setProfileImageUrl(
                    body.get("profileImageUrl").toString());

        return userRepository.save(user);
    }

    // ── CHANGE password ───────────────────────────────────────────
    @Transactional
    public void changePassword(Long userId,
                               String oldPassword,
                               String newPassword) {
        User user = getProfile(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BadRequestException("Old password is incorrect");
        }
        if (newPassword.length() < 6) {
            throw new BadRequestException(
                    "New password must be at least 6 characters");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ══════════════════════════════════════════════════════════════
    // ADDRESS MANAGEMENT
    // ══════════════════════════════════════════════════════════════

    public List<Address> getAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Transactional
    public Address addAddress(Long userId, Map<String, Object> body) {
        User user = getProfile(userId);

        Boolean isDefault = body.containsKey("isDefault")
                && (Boolean) body.get("isDefault");

        // If new address is default — unset existing default
        if (isDefault) {
            addressRepository
                    .findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .name(body.get("name").toString())
                .phone(body.get("phone").toString())
                .line1(body.get("line1").toString())
                .line2(body.containsKey("line2")
                        ? body.get("line2").toString() : null)
                .city(body.get("city").toString())
                .state(body.get("state").toString())
                .pincode(body.get("pincode").toString())
                .addressType(body.containsKey("addressType")
                        ? Address.AddressType.valueOf(
                        body.get("addressType").toString()) : null)
                .isDefault(isDefault)
                .build();

        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(Long addressId,
                                 Long userId,
                                 Map<String, Object> body) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your address");
        }

        if (body.containsKey("name"))
            address.setName(body.get("name").toString());
        if (body.containsKey("phone"))
            address.setPhone(body.get("phone").toString());
        if (body.containsKey("line1"))
            address.setLine1(body.get("line1").toString());
        if (body.containsKey("line2"))
            address.setLine2(body.get("line2").toString());
        if (body.containsKey("city"))
            address.setCity(body.get("city").toString());
        if (body.containsKey("state"))
            address.setState(body.get("state").toString());
        if (body.containsKey("pincode"))
            address.setPincode(body.get("pincode").toString());

        if (body.containsKey("isDefault")
                && (Boolean) body.get("isDefault")) {
            // unset old default
            addressRepository
                    .findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
            address.setIsDefault(true);
        }

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your address");
        }
        addressRepository.delete(address);
    }

    // ══════════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ══════════════════════════════════════════════════════════════

    public List<Notification> getNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void markOneRead(Long notifId, Long userId) {
        Notification notif = notificationRepository
                .findById(notifId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found"));

        if (!notif.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your notification");
        }
        notif.setIsRead(true);
        notificationRepository.save(notif);
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository
                .countByUserIdAndIsReadFalse(userId);
    }

    // ══════════════════════════════════════════════════════════════
    // RECENTLY VIEWED
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public void trackView(Long userId, Long productId) {
        // If already viewed — just update timestamp
        recentlyViewedRepository
                .findByUserIdAndProductId(userId, productId)
                .ifPresentOrElse(
                        rv -> {
                            // delete and re-insert to bump timestamp
                            recentlyViewedRepository.delete(rv);
                        },
                        () -> {
                            // Keep max 20 — delete oldest if exceeded
                            Long count = recentlyViewedRepository
                                    .countByUserId(userId);
                            if (count >= 20) {
                                List<RecentlyViewed> list =
                                        recentlyViewedRepository
                                                .findTop20ByUserIdOrderByViewedAtDesc(
                                                        userId);
                                // delete the last (oldest) one
                                recentlyViewedRepository.delete(
                                        list.get(list.size() - 1));
                            }
                        }
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        RecentlyViewed rv = RecentlyViewed.builder()
                .user(user)
                .product(product)
                .build();
        recentlyViewedRepository.save(rv);
    }

    public List<RecentlyViewed> getRecentlyViewed(Long userId) {
        return recentlyViewedRepository
                .findTop20ByUserIdOrderByViewedAtDesc(userId);
    }
}