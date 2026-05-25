package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;
    private final BannerRepository bannerRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    // ══════════════════════════════════════════════════════════════
    // USER MANAGEMENT
    // ══════════════════════════════════════════════════════════════

    public Page<User> getAllUsers(int page, int size) {
        return userRepository.findAll(
                PageRequest.of(page, size,
                        Sort.by("createdAt").descending()));
    }

    @Transactional
    public User toggleUserBan(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setIsActive(!user.getIsActive());
        User saved = userRepository.save(user);

        // Audit log
        logAction(adminId,
                user.getIsActive()
                        ? AuditLog.AuditAction.UNBAN_USER
                        : AuditLog.AuditAction.BAN_USER,
                "User", userId);

        return saved;
    }

    // ══════════════════════════════════════════════════════════════
    // VENDOR MANAGEMENT
    // ══════════════════════════════════════════════════════════════

    public List<Vendor> getPendingVendors() {
        return vendorRepository.findByIsApprovedFalse();
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    @Transactional
    public Vendor approveVendor(Long vendorId, Long adminId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        vendor.setIsApproved(true);

        // Update user role to VENDOR
        User user = vendor.getUser();
        user.setRole(User.Role.VENDOR);
        userRepository.save(user);

        // Send approval email
        emailService.sendVendorApprovalEmail(
                user.getEmail(), vendor.getShopName());

        // Notify vendor
        Notification notif = Notification.builder()
                .user(user)
                .type(Notification.NotifType.SYSTEM)
                .title("Seller Account Approved!")
                .message("Your shop '"
                        + vendor.getShopName()
                        + "' is now live.")
                .isRead(false)
                .build();
        notificationRepository.save(notif);

        logAction(adminId,
                AuditLog.AuditAction.APPROVE_VENDOR,
                "Vendor", vendorId);

        return vendorRepository.save(vendor);
    }

    @Transactional
    public Vendor rejectVendor(Long vendorId, Long adminId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vendor not found"));

        vendor.setIsApproved(false);
        vendor.setIsActive(false);

        logAction(adminId,
                AuditLog.AuditAction.REJECT_VENDOR,
                "Vendor", vendorId);

        return vendorRepository.save(vendor);
    }

    // ══════════════════════════════════════════════════════════════
    // COUPON MANAGEMENT
    // ══════════════════════════════════════════════════════════════

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Transactional
    public Coupon createCoupon(Map<String, Object> body) {
        if (couponRepository.existsByCode(
                body.get("code").toString())) {
            throw new com.flipkart.clone.exception
                    .BadRequestException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(body.get("code").toString().toUpperCase())
                .discountType(Coupon.DiscountType.valueOf(
                        body.get("discountType").toString()))
                .discountValue(new java.math.BigDecimal(
                        body.get("discountValue").toString()))
                .maxDiscountAmount(body.containsKey("maxDiscountAmount")
                        ? new java.math.BigDecimal(
                        body.get("maxDiscountAmount").toString())
                        : null)
                .minOrderAmount(body.containsKey("minOrderAmount")
                        ? new java.math.BigDecimal(
                        body.get("minOrderAmount").toString())
                        : null)
                .maxUses(body.containsKey("maxUses")
                        ? Integer.valueOf(body.get("maxUses").toString())
                        : null)
                .usedCount(0)
                .isActive(true)
                .expiresAt(body.containsKey("expiresAt")
                        ? java.time.LocalDateTime.parse(
                        body.get("expiresAt").toString())
                        : null)
                .build();

        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon toggleCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Coupon not found"));
        coupon.setIsActive(!coupon.getIsActive());
        return couponRepository.save(coupon);
    }

    // ══════════════════════════════════════════════════════════════
    // BANNER MANAGEMENT
    // ══════════════════════════════════════════════════════════════

    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    @Transactional
    public Banner createBanner(Map<String, Object> body) {
        Banner banner = Banner.builder()
                .title(body.get("title").toString())
                .imageUrl(body.get("imageUrl").toString())
                .redirectUrl(body.containsKey("redirectUrl")
                        ? body.get("redirectUrl").toString() : null)
                .position(Banner.BannerPosition.valueOf(
                        body.getOrDefault("position", "TOP").toString()))
                .isActive(true)
                .sortOrder(body.containsKey("sortOrder")
                        ? Integer.valueOf(
                        body.get("sortOrder").toString()) : 0)
                .build();
        return bannerRepository.save(banner);
    }

    @Transactional
    public void deleteBanner(Long bannerId) {
        bannerRepository.deleteById(bannerId);
    }

    // ══════════════════════════════════════════════════════════════
    // PLATFORM ANALYTICS
    // ══════════════════════════════════════════════════════════════

    public Map<String, Object> getPlatformAnalytics() {
        long totalUsers    = userRepository.count();
        long totalVendors  = vendorRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders   = orderRepository.count();

        java.math.BigDecimal totalRevenue = orderRepository
                .findAll()
                .stream()
                .filter(o -> o.getPaymentStatus() ==
                        Order.PaymentStatus.SUCCESS)
                .map(Order::getFinalAmount)
                .reduce(java.math.BigDecimal.ZERO,
                        java.math.BigDecimal::add);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalUsers",    totalUsers);
        analytics.put("totalVendors",  totalVendors);
        analytics.put("totalProducts", totalProducts);
        analytics.put("totalOrders",   totalOrders);
        analytics.put("totalRevenue",  totalRevenue);
        return analytics;
    }

    // ── Audit log helper ──────────────────────────────────────────
    private void logAction(Long adminId,
                           AuditLog.AuditAction action,
                           String entityType,
                           Long entityId) {
        User admin = userRepository.findById(adminId)
                .orElse(null);

        AuditLog log = AuditLog.builder()
                .actorUser(admin)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .build();
        auditLogRepository.save(log);
    }
}