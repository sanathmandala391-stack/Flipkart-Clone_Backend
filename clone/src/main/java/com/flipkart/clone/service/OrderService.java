//package com.flipkart.clone.service;
//
//import com.flipkart.clone.entity.*;
//import com.flipkart.clone.exception.BadRequestException;
//import com.flipkart.clone.exception.ResourceNotFoundException;
//import com.flipkart.clone.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class OrderService {
//
//    private final OrderRepository orderRepository;
//    private final OrderItemRepository orderItemRepository;
//    private final CartItemRepository cartItemRepository;
//    private final AddressRepository addressRepository;
//    private final UserRepository userRepository;
//    private final ProductVariantRepository variantRepository;
//    private final CouponRepository couponRepository;
//    private final CouponUsageRepository couponUsageRepository;
//    private final NotificationRepository notificationRepository;
//    private final EmailService emailService;
//
//    // ── PLACE ORDER (checkout) ────────────────────────────────────
//    @Transactional
//    public Order placeOrder(Long userId, Long addressId,
//                            String paymentMethod,
//                            String couponCode) {
//
//        // 1. Get cart items
//        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
//        if (cartItems.isEmpty()) {
//            throw new BadRequestException("Cart is empty");
//        }
//
//        // 2. Get delivery address
//        Address address = addressRepository.findById(addressId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Address not found"));
//
//        if (!address.getUser().getId().equals(userId)) {
//            throw new BadRequestException("Invalid address");
//        }
//
//        // 3. Calculate totals
//        BigDecimal totalAmount = BigDecimal.ZERO;
//        BigDecimal mrpTotal    = BigDecimal.ZERO;
//
//        for (CartItem item : cartItems) {
//            ProductVariant variant = item.getProductVariant();
//
//            // Check stock again at order time
//            if (variant.getStockQty() < item.getQuantity()) {
//                throw new BadRequestException(
//                        "Insufficient stock for: "
//                                + variant.getProduct().getName());
//            }
//            totalAmount = totalAmount.add(
//                    variant.getPrice().multiply(
//                            BigDecimal.valueOf(item.getQuantity())));
//            mrpTotal = mrpTotal.add(
//                    variant.getMrp().multiply(
//                            BigDecimal.valueOf(item.getQuantity())));
//        }
//
//        // 4. Apply coupon if provided
//        BigDecimal discountAmount = BigDecimal.ZERO;
//        Coupon coupon = null;
//
//        if (couponCode != null && !couponCode.isBlank()) {
//            coupon = couponRepository
//                    .findByCodeAndIsActiveTrue(couponCode)
//                    .orElseThrow(() ->
//                            new BadRequestException("Invalid coupon"));
//
//            if (couponUsageRepository
//                    .existsByCouponIdAndUserId(coupon.getId(), userId)) {
//                throw new BadRequestException(
//                        "Coupon already used");
//            }
//
//            if (coupon.getDiscountType() == Coupon.DiscountType.FLAT) {
//                discountAmount = coupon.getDiscountValue();
//            } else {
//                discountAmount = totalAmount
//                        .multiply(coupon.getDiscountValue())
//                        .divide(BigDecimal.valueOf(100));
//
//                if (coupon.getMaxDiscountAmount() != null &&
//                        discountAmount.compareTo(
//                                coupon.getMaxDiscountAmount()) > 0) {
//                    discountAmount = coupon.getMaxDiscountAmount();
//                }
//            }
//        }
//
//        // 5. Delivery charge
//        BigDecimal deliveryCharge =
//                totalAmount.compareTo(BigDecimal.valueOf(500)) >= 0
//                        ? BigDecimal.ZERO : BigDecimal.valueOf(40);
//
//        BigDecimal finalAmount = totalAmount
//                .subtract(discountAmount)
//                .add(deliveryCharge);
//
//        // 6. Build Order entity
//        User user = userRepository.findById(userId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("User not found"));
//
//        Order order = Order.builder()
//                .user(user)
//                .address(address)
//                .status(Order.OrderStatus.PENDING)
//                .totalAmount(totalAmount)
//                .discountAmount(discountAmount)
//                .deliveryCharge(deliveryCharge)
//                .finalAmount(finalAmount)
//                .paymentMethod(Order.PaymentMethod.valueOf(
//                        paymentMethod.toUpperCase()))
//                .paymentStatus(Order.PaymentStatus.PENDING)
//                .couponCode(couponCode)
//                .build();
//
//        Order savedOrder = orderRepository.save(order);
//
//        // 7. Build OrderItems + decrement stock
//        List<OrderItem> orderItems = new ArrayList<>();
//        for (CartItem cartItem : cartItems) {
//            ProductVariant variant = cartItem.getProductVariant();
//
//            OrderItem orderItem = OrderItem.builder()
//                    .order(savedOrder)
//                    .productVariant(variant)
//                    .vendor(variant.getProduct().getVendor())
//                    .quantity(cartItem.getQuantity())
//                    .unitPrice(variant.getPrice())
//                    .totalPrice(variant.getPrice().multiply(
//                            BigDecimal.valueOf(cartItem.getQuantity())))
//                    .status(OrderItem.ItemStatus.CONFIRMED)
//                    .build();
//
//            orderItems.add(orderItem);
//
//            // Decrement stock
//            variant.setStockQty(
//                    variant.getStockQty() - cartItem.getQuantity());
//            variantRepository.save(variant);
//        }
//        orderItemRepository.saveAll(orderItems);
//
//        // 8. Record coupon usage
//        if (coupon != null) {
//            CouponUsage usage = CouponUsage.builder()
//                    .coupon(coupon)
//                    .user(user)
//                    .order(savedOrder)
//                    .discountApplied(discountAmount)
//                    .build();
//            couponUsageRepository.save(usage);
//
//            // Increment coupon used count
//            coupon.setUsedCount(coupon.getUsedCount() + 1);
//            couponRepository.save(coupon);
//        }
//
//        // 9. Clear cart
//        cartItemRepository.deleteByUserId(userId);
//
//        // 10. Send notification
//        Notification notification = Notification.builder()
//                .user(user)
//                .type(Notification.NotifType.ORDER)
//                .title("Order Placed!")
//                .message("Your order #" + savedOrder.getId()
//                        + " has been placed successfully.")
//                .isRead(false)
//                .build();
//        notificationRepository.save(notification);
//
//        // 11. Send email
//        emailService.sendOrderConfirmation(
//                user.getEmail(),
//                savedOrder.getId().toString(),
//                finalAmount.toString()
//        );
//
//        return savedOrder;
//    }
//
//    // ── GET my orders ─────────────────────────────────────────────
//    public Page<Order> getMyOrders(Long userId, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return orderRepository
//                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
//    }
//
//    // ── GET order by ID ───────────────────────────────────────────
//    public Order getOrderById(Long orderId, Long userId) {
////        Order order = orderRepository.findById(orderId)
//        Order order = orderRepository.findByIdWithItems(orderId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Order not found"));
//
//        // Users can only see their own orders
//        if (!order.getUser().getId().equals(userId)) {
//            throw new BadRequestException(
//                    "You are not authorized to view this order");
//        }
//        return order;
//    }
//
//    // ── CANCEL order ──────────────────────────────────────────────
//    @Transactional
//    public Order cancelOrder(Long orderId, Long userId) {
//        Order order = getOrderById(orderId, userId);
//
//        // Can only cancel PENDING or CONFIRMED orders
//        if (order.getStatus() != Order.OrderStatus.PENDING &&
//                order.getStatus() != Order.OrderStatus.CONFIRMED) {
//            throw new BadRequestException(
//                    "Order cannot be cancelled at this stage");
//        }
//
//        order.setStatus(Order.OrderStatus.CANCELLED);
//
//        // Restock items
//        order.getOrderItems().forEach(item -> {
//            ProductVariant variant = item.getProductVariant();
//            variant.setStockQty(
//                    variant.getStockQty() + item.getQuantity());
//            variantRepository.save(variant);
//        });
//
//        // Notify user
//        Notification notification = Notification.builder()
//                .user(order.getUser())
//                .type(Notification.NotifType.ORDER)
//                .title("Order Cancelled")
//                .message("Your order #" + orderId
//                        + " has been cancelled.")
//                .isRead(false)
//                .build();
//        notificationRepository.save(notification);
//
//        return orderRepository.save(order);
//    }
//
//    // ── VENDOR: get orders to fulfill ─────────────────────────────
//    public List<OrderItem> getVendorOrders(Long userId) {
//        return orderItemRepository.findByVendorId(userId);
//    }
//
//    // ── VENDOR: update order item status ─────────────────────────
//    @Transactional
//    public OrderItem updateItemStatus(Long orderItemId,
//                                      Long userId,
//                                      String status) {
//
//        OrderItem item = orderItemRepository.findById(orderItemId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Order item not found"));
//
//        if (!item.getVendor().getUser().getId().equals(userId)) {
//            throw new BadRequestException("Not authorized");
//        }
//
//        item.setStatus(OrderItem.ItemStatus.valueOf(
//                status.toUpperCase()));
//        return orderItemRepository.save(item);
//    }
//
//    // ── ADMIN: get all orders ─────────────────────────────────────
//    public Page<Order> getAllOrders(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return orderRepository.findAllByOrderByCreatedAtDesc(pageable);
//    }
//
//    // ── ADMIN: update order status ────────────────────────────────
//    @Transactional
//    public Order updateOrderStatus(Long orderId, String status) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Order not found"));
//
//        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
//
//        // Notify user on delivery
//        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
//            order.setPaymentStatus(Order.PaymentStatus.SUCCESS);
//            Notification notification = Notification.builder()
//                    .user(order.getUser())
//                    .type(Notification.NotifType.ORDER)
//                    .title("Order Delivered!")
//                    .message("Your order #" + orderId
//                            + " has been delivered.")
//                    .isRead(false)
//                    .build();
//            notificationRepository.save(notification);
//        }
//
//        return orderRepository.save(order);
//    }
//}







//package com.flipkart.clone.service;
//
//import com.flipkart.clone.entity.*;
//import com.flipkart.clone.exception.BadRequestException;
//import com.flipkart.clone.exception.ResourceNotFoundException;
//import com.flipkart.clone.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class OrderService {
//
//    private final OrderRepository orderRepository;
//    private final OrderItemRepository orderItemRepository;
//    private final CartItemRepository cartItemRepository;
//    private final AddressRepository addressRepository;
//    private final UserRepository userRepository;
//    private final ProductVariantRepository variantRepository;
//    private final CouponRepository couponRepository;
//    private final CouponUsageRepository couponUsageRepository;
//    private final NotificationRepository notificationRepository;
//    private final EmailService emailService;
//
//    // ── PLACE ORDER ────────────────────────────────────
//    @Transactional
//    public Order placeOrder(Long userId, Long addressId,
//                            String paymentMethod,
//                            String couponCode) {
//
//        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
//        if (cartItems.isEmpty()) {
//            throw new BadRequestException("Cart is empty");
//        }
//
//        Address address = addressRepository.findById(addressId)
//                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
//
//        if (!address.getUser().getId().equals(userId)) {
//            throw new BadRequestException("Invalid address");
//        }
//
//        BigDecimal totalAmount = BigDecimal.ZERO;
//
//        for (CartItem item : cartItems) {
//            ProductVariant variant = item.getProductVariant();
//
//            if (variant.getStockQty() < item.getQuantity()) {
//                throw new BadRequestException(
//                        "Insufficient stock for: " + variant.getProduct().getName());
//            }
//
//            totalAmount = totalAmount.add(
//                    variant.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
//            );
//        }
//
//        BigDecimal discountAmount = BigDecimal.ZERO;
//
//        if (couponCode != null && !couponCode.isBlank()) {
//            Coupon coupon = couponRepository
//                    .findByCodeAndIsActiveTrue(couponCode)
//                    .orElseThrow(() -> new BadRequestException("Invalid coupon"));
//
//            if (couponUsageRepository.existsByCouponIdAndUserId(coupon.getId(), userId)) {
//                throw new BadRequestException("Coupon already used");
//            }
//
//            if (coupon.getDiscountType() == Coupon.DiscountType.FLAT) {
//                discountAmount = coupon.getDiscountValue();
//            } else {
//                discountAmount = totalAmount
//                        .multiply(coupon.getDiscountValue())
//                        .divide(BigDecimal.valueOf(100));
//            }
//        }
//
//        BigDecimal deliveryCharge =
//                totalAmount.compareTo(BigDecimal.valueOf(500)) >= 0
//                        ? BigDecimal.ZERO
//                        : BigDecimal.valueOf(40);
//
//        BigDecimal finalAmount = totalAmount
//                .subtract(discountAmount)
//                .add(deliveryCharge);
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        Order order = Order.builder()
//                .user(user)
//                .address(address)
//                .status(Order.OrderStatus.PENDING)
//                .totalAmount(totalAmount)
//                .discountAmount(discountAmount)
//                .deliveryCharge(deliveryCharge)
//                .finalAmount(finalAmount)
//                .paymentMethod(Order.PaymentMethod.valueOf(paymentMethod.toUpperCase()))
//                .paymentStatus(Order.PaymentStatus.PENDING)
//                .couponCode(couponCode)
//                .build();
//
//        Order savedOrder = orderRepository.save(order);
//
//        List<OrderItem> orderItems = new ArrayList<>();
//
//        for (CartItem cartItem : cartItems) {
//            ProductVariant variant = cartItem.getProductVariant();
//
//            OrderItem item = OrderItem.builder()
//                    .order(savedOrder)
//                    .productVariant(variant)
//                    .vendor(variant.getProduct().getVendor())
//                    .quantity(cartItem.getQuantity())
//                    .unitPrice(variant.getPrice())
//                    .totalPrice(variant.getPrice()
//                            .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
//                    .status(OrderItem.ItemStatus.CONFIRMED)
//                    .build();
//
//            orderItems.add(item);
//
//            variant.setStockQty(variant.getStockQty() - cartItem.getQuantity());
//            variantRepository.save(variant);
//        }
//
//        orderItemRepository.saveAll(orderItems);
//        cartItemRepository.deleteByUserId(userId);
//
//        return savedOrder;
//    }
//
//    // ── GET ORDER BY ID (🔥 FIXED) ─────────────────────────
//    public Order getOrderById(Long orderId, Long userId) {
//
//        Order order = orderRepository.findByIdWithItems(orderId)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("Order not found"));
//
//        if (!order.getUser().getId().equals(userId)) {
//            throw new BadRequestException("Not authorized");
//        }
//
//        return order;
//    }
//
//    // ── GET MY ORDERS ─────────────────────────────────────
//    public Page<Order> getMyOrders(Long userId, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
//    }
//
//    // ── CANCEL ORDER ──────────────────────────────────────
//    @Transactional
//    public Order cancelOrder(Long orderId, Long userId) {
//
//        Order order = getOrderById(orderId, userId);
//
//        if (order.getStatus() != Order.OrderStatus.PENDING &&
//                order.getStatus() != Order.OrderStatus.CONFIRMED) {
//            throw new BadRequestException("Cannot cancel now");
//        }
//
//        order.setStatus(Order.OrderStatus.CANCELLED);
//
//        order.getOrderItems().forEach(item -> {
//            ProductVariant variant = item.getProductVariant();
//            variant.setStockQty(variant.getStockQty() + item.getQuantity());
//            variantRepository.save(variant);
//        });
//
//        return orderRepository.save(order);
//    }
//
//    // ── ADMIN / OTHER METHODS (UNCHANGED) ─────────────────
//    public List<OrderItem> getVendorOrders(Long userId) {
//        return orderItemRepository.findByVendorId(userId);
//    }
//
//    @Transactional
//    public OrderItem updateItemStatus(Long orderItemId,
//                                      Long userId,
//                                      String status) {
//
//        OrderItem item = orderItemRepository.findById(orderItemId)
//                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
//
//        if (!item.getVendor().getUser().getId().equals(userId)) {
//            throw new BadRequestException("Not authorized");
//        }
//
//        item.setStatus(OrderItem.ItemStatus.valueOf(status.toUpperCase()));
//        return orderItemRepository.save(item);
//    }
//
//    public Page<Order> getAllOrders(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return orderRepository.findAllByOrderByCreatedAtDesc(pageable);
//    }
//
//    @Transactional
//    public Order updateOrderStatus(Long orderId, String status) {
//
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
//
//        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
//
//        return orderRepository.save(order);
//    }
//}































































package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository           orderRepository;
    private final OrderItemRepository       orderItemRepository;
    private final CartItemRepository        cartItemRepository;
    private final AddressRepository         addressRepository;
    private final UserRepository            userRepository;
    private final ProductVariantRepository  variantRepository;
    private final CouponRepository          couponRepository;
    private final CouponUsageRepository     couponUsageRepository;
    private final NotificationRepository    notificationRepository;
    private final EmailService              emailService;

    // ✅ Exotel voice call service
    private final ExotelService             exotelService;

    // ── PLACE ORDER ────────────────────────────────────────────
    @Transactional
    public Order placeOrder(Long userId, Long addressId,
                            String paymentMethod,
                            String couponCode) {

        List<CartItem> cartItems =
                cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("Invalid address");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();
            if (variant.getStockQty() < item.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for: " +
                                variant.getProduct().getName());
            }
            totalAmount = totalAmount.add(
                    variant.getPrice().multiply(
                            BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository
                    .findByCodeAndIsActiveTrue(couponCode)
                    .orElseThrow(() ->
                            new BadRequestException("Invalid coupon"));

            if (couponUsageRepository
                    .existsByCouponIdAndUserId(
                            coupon.getId(), userId)) {
                throw new BadRequestException("Coupon already used");
            }

            if (coupon.getDiscountType() ==
                    Coupon.DiscountType.FLAT) {
                discountAmount = coupon.getDiscountValue();
            } else {
                discountAmount = totalAmount
                        .multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));
            }
        }

        BigDecimal deliveryCharge =
                totalAmount.compareTo(BigDecimal.valueOf(500)) >= 0
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(40);

        BigDecimal finalAmount = totalAmount
                .subtract(discountAmount)
                .add(deliveryCharge);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Order order = Order.builder()
                .user(user)
                .address(address)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .deliveryCharge(deliveryCharge)
                .finalAmount(finalAmount)
                .paymentMethod(Order.PaymentMethod.valueOf(
                        paymentMethod.toUpperCase()))
                .paymentStatus(Order.PaymentStatus.PENDING)
                .couponCode(couponCode)
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getProductVariant();
            OrderItem item = OrderItem.builder()
                    .order(savedOrder)
                    .productVariant(variant)
                    .vendor(variant.getProduct().getVendor())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(variant.getPrice())
                    .totalPrice(variant.getPrice().multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())))
                    .status(OrderItem.ItemStatus.CONFIRMED)
                    .build();
            orderItems.add(item);
            variant.setStockQty(
                    variant.getStockQty() - cartItem.getQuantity());
            variantRepository.save(variant);
        }

        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteByUserId(userId);

        // ✅ Make voice call — runs in background so API is fast
        final String phone      = address.getPhone();
        final String name       = user.getName();
        final Long   orderId    = savedOrder.getId();
        final String amount     = savedOrder.getFinalAmount()
                .toPlainString();
        CompletableFuture.runAsync(() ->
                exotelService.callOrderPlaced(phone, name, orderId, amount)
        );

        return savedOrder;
    }

    // ── GET ORDER BY ID ────────────────────────────────────────
    public Order getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized");
        }
        return order;
    }

    // ── GET MY ORDERS ──────────────────────────────────────────
    public Page<Order> getMyOrders(Long userId,
                                   int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    // ── CANCEL ORDER ───────────────────────────────────────────
    @Transactional
    public Order cancelOrder(Long orderId, Long userId) {
        Order order = getOrderById(orderId, userId);

        if (order.getStatus() != Order.OrderStatus.PENDING &&
                order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel now");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);

        order.getOrderItems().forEach(item -> {
            ProductVariant variant = item.getProductVariant();
            variant.setStockQty(
                    variant.getStockQty() + item.getQuantity());
            variantRepository.save(variant);
        });

        Order saved = orderRepository.save(order);

        // ✅ Voice call — order cancelled
        final String phone = order.getAddress().getPhone();
        final String name  = order.getUser().getName();
        CompletableFuture.runAsync(() ->
                exotelService.callOrderCancelled(phone, name, orderId)
        );

        return saved;
    }

    // ── ADMIN: UPDATE ORDER STATUS ─────────────────────────────
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        order.setStatus(Order.OrderStatus.valueOf(
                status.toUpperCase()));
        Order saved = orderRepository.save(order);

        // ✅ Voice call based on new status
        final String phone = order.getAddress().getPhone();
        final String name  = order.getUser().getName();

        CompletableFuture.runAsync(() -> {
            switch (status.toUpperCase()) {
                case "SHIPPED" ->
                        exotelService.callOrderShipped(
                                phone, name, orderId);
                case "OUT_FOR_DELIVERY" ->
                        exotelService.callOutForDelivery(
                                phone, name, orderId);
                case "DELIVERED" ->
                        exotelService.callOrderDelivered(
                                phone, name, orderId);
                case "CANCELLED" ->
                        exotelService.callOrderCancelled(
                                phone, name, orderId);
            }
        });

        return saved;
    }

    // ── VENDOR / ADMIN UNCHANGED METHODS ──────────────────────
    public List<OrderItem> getVendorOrders(Long userId) {
        return orderItemRepository.findByVendorId(userId);
    }

    @Transactional
    public OrderItem updateItemStatus(Long orderItemId,
                                      Long userId,
                                      String status) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order item not found"));
        if (!item.getVendor().getUser().getId().equals(userId)) {
            throw new BadRequestException("Not authorized");
        }
        item.setStatus(OrderItem.ItemStatus.valueOf(
                status.toUpperCase()));
        return orderItemRepository.save(item);
    }

    public Page<Order> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}