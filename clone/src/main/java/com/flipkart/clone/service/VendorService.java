package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final VendorPayoutRepository payoutRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ShipmentRepository shipmentRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    // ── REGISTER as vendor ────────────────────────────────────────
    @Transactional
    public Vendor registerVendor(Long userId,
                                 Map<String, Object> body) {

        if (vendorRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException(
                    "You already have a vendor account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Vendor vendor = Vendor.builder()
                .user(user)
                .shopName(body.get("shopName").toString())
                .shopDescription(body.containsKey("shopDescription")
                        ? body.get("shopDescription").toString() : null)
                .gstNumber(body.containsKey("gstNumber")
                        ? body.get("gstNumber").toString() : null)
                .shopAddress(body.containsKey("shopAddress")
                        ? body.get("shopAddress").toString() : null)
                .bankAccountNumber(body.containsKey("bankAccountNumber")
                        ? body.get("bankAccountNumber").toString() : null)
                .bankIfsc(body.containsKey("bankIfsc")
                        ? body.get("bankIfsc").toString() : null)
                .bankHolderName(body.containsKey("bankHolderName")
                        ? body.get("bankHolderName").toString() : null)
                .isApproved(false)
                .isActive(true)
                .rating(0.0)
                .totalSales(0)
                .build();

        return vendorRepository.save(vendor);
    }

    // ── GET vendor profile ────────────────────────────────────────
    public Vendor getVendorProfile(Long userId) {
        return vendorRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vendor profile not found"));
    }

    // ── UPDATE vendor profile ─────────────────────────────────────
    @Transactional
    public Vendor updateProfile(Long userId,
                                Map<String, Object> body) {
        Vendor vendor = getVendorProfile(userId);

        if (body.containsKey("shopName"))
            vendor.setShopName(body.get("shopName").toString());
        if (body.containsKey("shopDescription"))
            vendor.setShopDescription(
                    body.get("shopDescription").toString());
        if (body.containsKey("shopAddress"))
            vendor.setShopAddress(body.get("shopAddress").toString());
        if (body.containsKey("bankAccountNumber"))
            vendor.setBankAccountNumber(
                    body.get("bankAccountNumber").toString());
        if (body.containsKey("bankIfsc"))
            vendor.setBankIfsc(body.get("bankIfsc").toString());
        if (body.containsKey("bankHolderName"))
            vendor.setBankHolderName(
                    body.get("bankHolderName").toString());

        return vendorRepository.save(vendor);
    }

    // ── GET vendor orders ─────────────────────────────────────────
    public List<OrderItem> getOrders(Long userId) {
        Vendor vendor = getVendorProfile(userId);
        return orderItemRepository.findByVendorId(vendor.getId());
    }

    // ── SHIP an order item ────────────────────────────────────────
    @Transactional
    public Shipment shipOrder(Long orderId,
                              Long userId,
                              Map<String, Object> body) {

        getVendorProfile(userId); // verify vendor exists

        Order order = new Order();
        order.setId(orderId);

        // Check if shipment already exists
        if (shipmentRepository.findByOrderId(orderId).isPresent()) {
            throw new BadRequestException(
                    "Shipment already created for this order");
        }

        Shipment shipment = Shipment.builder()
                .order(order)
                .courierName(body.get("courierName").toString())
                .trackingNumber(body.get("trackingNumber").toString())
                .status(Shipment.ShipmentStatus.SHIPPED)
                .build();

        if (body.containsKey("estimatedDelivery")) {
            shipment.setEstimatedDelivery(
                    java.time.LocalDate.parse(
                            body.get("estimatedDelivery").toString()));
        }

        shipment.setShippedAt(java.time.LocalDateTime.now());
        return shipmentRepository.save(shipment);
    }

    // ── GET analytics ─────────────────────────────────────────────
    public Map<String, Object> getAnalytics(Long userId) {
        Vendor vendor = getVendorProfile(userId);

        List<Product> products = productRepository
                .findByVendorIdAndIsActiveTrue(vendor.getId());

        List<OrderItem> orders = orderItemRepository
                .findByVendorId(vendor.getId());

        long totalOrders   = orders.size();
        long deliveredOrders = orders.stream()
                .filter(o -> o.getStatus() ==
                        OrderItem.ItemStatus.DELIVERED)
                .count();

        java.math.BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() ==
                        OrderItem.ItemStatus.DELIVERED)
                .map(OrderItem::getTotalPrice)
                .reduce(java.math.BigDecimal.ZERO,
                        java.math.BigDecimal::add);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalProducts",    products.size());
        analytics.put("totalOrders",      totalOrders);
        analytics.put("deliveredOrders",  deliveredOrders);
        analytics.put("totalRevenue",     totalRevenue);
        analytics.put("shopRating",       vendor.getRating());
        analytics.put("totalSales",       vendor.getTotalSales());
        return analytics;
    }

    // ── GET payout history ────────────────────────────────────────
    public List<VendorPayout> getPayouts(Long userId) {
        Vendor vendor = getVendorProfile(userId);
        return payoutRepository
                .findByVendorIdOrderByCreatedAtDesc(vendor.getId());
    }

    // ── GET return requests for vendor ────────────────────────────
    public List<ReturnRequest> getReturnRequests(Long userId) {
        Vendor vendor = getVendorProfile(userId);
        List<OrderItem> items = orderItemRepository
                .findByVendorId(vendor.getId());

        return items.stream()
                .map(item -> returnRequestRepository
                        .findByOrderItemId(item.getId())
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}