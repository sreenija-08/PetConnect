package com.petconnect.service;

import com.petconnect.entity.Notification;
import com.petconnect.entity.User;

import java.util.List;

public interface NotificationService {
    Notification createNotification(User recipient, String title, String message, String type);
    List<Notification> getUserNotifications(User user);
    long getUnreadCount(User user);
    void markAsRead(Long notificationId);
    void markAllAsRead(User user);
}
