package com.flipkart.clone.service;

import com.flipkart.clone.entity.Notification;
import com.flipkart.clone.entity.User;
import com.flipkart.clone.exception.ResourceNotFoundException;
import com.flipkart.clone.repository.NotificationRepository;
import com.flipkart.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ── GET all notifications ─────────────────────────────────────
    public List<Notification> getAll(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ── GET unread notifications ──────────────────────────────────
    public List<Notification> getUnread(Long userId) {
        return notificationRepository
                .findByUserIdAndIsReadFalse(userId);
    }

    // ── GET unread count ──────────────────────────────────────────
    public Long getUnreadCount(Long userId) {
        return notificationRepository
                .countByUserIdAndIsReadFalse(userId);
    }

    // ── MARK all as read ──────────────────────────────────────────
    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    // ── MARK one as read ──────────────────────────────────────────
    @Transactional
    public void markOneRead(Long notifId) {
        Notification notif = notificationRepository
                .findById(notifId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found"));
        notif.setIsRead(true);
        notificationRepository.save(notif);
    }

    // ── SEND notification to a user ───────────────────────────────
    @Transactional
    public Notification send(Long userId,
                             Notification.NotifType type,
                             String title,
                             String message) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Notification notif = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        return notificationRepository.save(notif);
    }

    // ── DELETE notification ───────────────────────────────────────
    @Transactional
    public void delete(Long notifId) {
        notificationRepository.deleteById(notifId);
    }
}