package com.flipkart.clone.service;

import com.flipkart.clone.entity.*;
import com.flipkart.clone.exception.BadRequestException;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ProductVariantRepository variantRepository;

    // USER — raise return
    @Transactional
    public ReturnRequest raiseReturn(Long userId,
                                     Long orderItemId,
                                     Map<String, Object> body) {

        OrderItem orderItem = orderItemRepository
                .findById(orderItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order item not found"));

        if (orderItem.getStatus() !=
                OrderItem.ItemStatus.DELIVERED) {
            throw new BadRequestException(
                    "Only delivered items can be returned");
        }

        if (returnRequestRepository
                .findByOrderItemId(orderItemId).isPresent()) {
            throw new BadRequestException(
                    "Return already raised for this item");
        }

        if (!orderItem.getOrder().getUser()
                .getId().equals(userId)) {
            throw new BadRequestException("Not authorized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        ReturnRequest returnRequest = ReturnRequest.builder()
                .orderItem(orderItem)
                .user(user)
                .reason(ReturnRequest.ReturnReason.valueOf(
                        body.get("reason").toString()))
                .description(body.containsKey("description")
                        ? body.get("description").toString()
                        : null)
                .imagesJson(body.containsKey("imagesJson")
                        ? body.get("imagesJson").toString()
                        : null)
                .status(ReturnRequest.ReturnStatus.PENDING)
                .refundAmount(orderItem.getTotalPrice())
                .build();

        ReturnRequest saved =
                returnRequestRepository.save(returnRequest);

        // Update item status
        orderItem.setStatus(
                OrderItem.ItemStatus.RETURN_REQUESTED);
        orderItemRepository.save(orderItem);

        // Notify user
        Notification notif = Notification.builder()
                .user(user)
                .type(Notification.NotifType.RETURN)
                .title("Return Request Submitted")
                .message("Your return request has been " +
                        "submitted and will be processed " +
                        "within 48 hours.")
                .isRead(false)
                .build();
        notificationRepository.save(notif);

        return saved;
    }

    // USER — my returns
    public List<ReturnRequest> getUserReturns(Long userId) {
        return returnRequestRepository.findByUserId(userId);
    }

    // ADMIN — all returns
    public List<ReturnRequest> getAllReturns() {
        return returnRequestRepository.findAll();
    }

    // ADMIN — pending returns
    public List<ReturnRequest> getPendingReturns() {
        return returnRequestRepository.findByStatus(
                ReturnRequest.ReturnStatus.PENDING);
    }

    // ADMIN — approve return
    @Transactional
    public ReturnRequest approveReturn(Long returnId) {
        ReturnRequest rr = returnRequestRepository
                .findById(returnId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Return request not found"));

        rr.setStatus(ReturnRequest.ReturnStatus.APPROVED);

        // Update item status
        OrderItem item = rr.getOrderItem();
        item.setStatus(OrderItem.ItemStatus.RETURNED);
        orderItemRepository.save(item);

        // Restock variant
        ProductVariant variant = item.getProductVariant();
        variant.setStockQty(
                variant.getStockQty() + item.getQuantity());
        variantRepository.save(variant);

        // Notify user
        Notification notif = Notification.builder()
                .user(rr.getUser())
                .type(Notification.NotifType.RETURN)
                .title("Return Approved!")
                .message("Your return has been approved. " +
                        "Refund of ₹" +
                        rr.getRefundAmount() +
                        " will be credited in 5-7 days.")
                .isRead(false)
                .build();
        notificationRepository.save(notif);

        return returnRequestRepository.save(rr);
    }

    // ADMIN — reject return
    @Transactional
    public ReturnRequest rejectReturn(Long returnId,
                                      String reason) {
        ReturnRequest rr = returnRequestRepository
                .findById(returnId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Return request not found"));

        rr.setStatus(ReturnRequest.ReturnStatus.REJECTED);

        // Notify user
        Notification notif = Notification.builder()
                .user(rr.getUser())
                .type(Notification.NotifType.RETURN)
                .title("Return Rejected")
                .message("Your return request was rejected. " +
                        "Reason: " + reason)
                .isRead(false)
                .build();
        notificationRepository.save(notif);

        return returnRequestRepository.save(rr);
    }
}