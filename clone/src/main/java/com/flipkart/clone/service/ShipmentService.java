package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final NotificationRepository notificationRepository;

    // GET shipment by order
    public Shipment getByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shipment not found for order: "
                                        + orderId));
    }

    // GET by tracking number — PUBLIC
    public Shipment trackByNumber(String trackingNumber) {
        return shipmentRepository
                .findByTrackingNumber(trackingNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No shipment found for tracking: "
                                        + trackingNumber));
    }

    // VENDOR — create shipment
    @Transactional
    public Shipment createShipment(Long orderId,
                                   Map<String, Object> body) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"));

        if (shipmentRepository
                .findByOrderId(orderId).isPresent()) {
            throw new BadRequestException(
                    "Shipment already exists for this order");
        }

        Shipment shipment = Shipment.builder()
                .order(order)
                .courierName(
                        body.get("courierName").toString())
                .trackingNumber(
                        body.get("trackingNumber").toString())
                .status(Shipment.ShipmentStatus.PACKED)
                .estimatedDelivery(
                        body.containsKey("estimatedDelivery")
                                ? LocalDate.parse(
                                body.get("estimatedDelivery")
                                        .toString())
                                : LocalDate.now().plusDays(5))
                .shippedAt(LocalDateTime.now())
                .build();

        Shipment saved = shipmentRepository.save(shipment);

        // Update order status to SHIPPED
        order.setStatus(Order.OrderStatus.SHIPPED);
        orderRepository.save(order);

        // Notify user
        Notification notif = Notification.builder()
                .user(order.getUser())
                .type(Notification.NotifType.ORDER)
                .title("Order Shipped!")
                .message("Your order #" + orderId +
                        " shipped via " +
                        shipment.getCourierName() +
                        ". Track: " +
                        shipment.getTrackingNumber())
                .isRead(false)
                .build();
        notificationRepository.save(notif);

        return saved;
    }

    // ADMIN — update shipment status
    @Transactional
    public Shipment updateStatus(Long shipmentId,
                                 String status) {

        Shipment shipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shipment not found"));

        Shipment.ShipmentStatus newStatus =
                Shipment.ShipmentStatus.valueOf(
                        status.toUpperCase());

        shipment.setStatus(newStatus);

        // If delivered — update order + items
        if (newStatus ==
                Shipment.ShipmentStatus.DELIVERED) {

            shipment.setDeliveredAt(LocalDateTime.now());

            Order order = shipment.getOrder();
            order.setStatus(Order.OrderStatus.DELIVERED);
            orderRepository.save(order);

            // Mark all order items DELIVERED
            order.getOrderItems().forEach(item -> {
                item.setStatus(
                        OrderItem.ItemStatus.DELIVERED);
                orderItemRepository.save(item);
            });

            // Notify user
            Notification notif = Notification.builder()
                    .user(order.getUser())
                    .type(Notification.NotifType.ORDER)
                    .title("Order Delivered!")
                    .message("Your order #" +
                            order.getId() +
                            " has been delivered. " +
                            "Enjoy your purchase!")
                    .isRead(false)
                    .build();
            notificationRepository.save(notif);
        }

        return shipmentRepository.save(shipment);
    }
}